/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v3.create

import api.models.domain.DateRange
import api.models.errors.*
import api.models.utils.JsonErrorValidators
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import api.support.IntegrationBaseSpec
import models.errors.*
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import v3.fixtures.CreateRequestFixtures.*
import v3.models.errors.CisDeductionsApiCommonErrors.{DeductionFromDateFormatError, DeductionToDateFormatError}

import java.time.LocalDate

class CreateControllerISpec extends IntegrationBaseSpec with JsonErrorValidators {

  "Calling the 'Create CIS Deductions for Subcontractor' endpoint" should {
    "return a 200 status code" when {
      "any valid non-TYS request is made" in new NonTysTest {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = DownstreamStub.POST,
          uri = downstreamUri,
          status = OK,
          body = createDeductionResponseBody
        )

        val response: WSResponse = await(request().post(requestJson))
        response.status shouldBe OK
        response.json shouldBe createDeductionResponseBody
        response.header("Content-Type") shouldBe Some("application/json")
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "any valid TYS request is made" in new TysTest {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = DownstreamStub.POST,
          uri = downstreamUri,
          status = CREATED,
          body = createDeductionResponseBody
        )

        val response: WSResponse = await(request().post(requestBodyJsonTys))
        response.status shouldBe OK
        response.json shouldBe createDeductionResponseBody
        response.header("Content-Type") shouldBe Some("application/json")
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String, body: JsValue, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest {
            override val nino: String = requestNino

            val response: WSResponse = await(request().post(body))
            response.status shouldBe expectedStatus
            response.json shouldBe expectedBody.asJson
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input: Seq[(String, JsValue, Int, MtdError)] = List(
          ("AA1123A", requestJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A", requestJsonErrorFromDate, BAD_REQUEST, FromDateFormatError.withPath("/fromDate")),
          ("AA123456A", requestJsonErrorToDate, BAD_REQUEST, ToDateFormatError.withPath("/toDate")),
          ("AA123456A", requestJsonErrorTaxYearNotSupported, BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", requestJsonCurrentTaxYear, BAD_REQUEST, RuleTaxYearNotEndedError),
          ("AA123456A", requestJsonErrorToDateBeforeFromDate, BAD_REQUEST, RuleDateRangeInvalidError),
          ("AA123456A", requestJsonErrorEmpRef, BAD_REQUEST, EmployerRefFormatError.withPath("/employerRef")),
          (
            "AA123456A",
            requestJson.update("/contractorName", JsString("a" * 106)),
            BAD_REQUEST,
            ContractorNameFormatError.withPath("/contractorName")
          ),
          (
            "AA123456A",
            requestJsonErrorDeductionFromDate,
            BAD_REQUEST,
            DeductionFromDateFormatError.withPaths(List("/periodData/0/deductionFromDate", "/periodData/1/deductionFromDate"))
          ),
          (
            "AA123456A",
            requestJsonErrorDeductionToDate,
            BAD_REQUEST,
            DeductionToDateFormatError.withPaths(List("/periodData/0/deductionToDate", "/periodData/1/deductionToDate"))
          ),
          (
            "AA123456A",
            requestJsonErrorDeductionPeriodsOutsideTaxYear,
            BAD_REQUEST,
            RuleUnalignedDeductionsPeriodError.withPaths(List("/periodData/0", "/periodData/1"))
          ),
          (
            "AA123456A",
            requestJsonErrorDeductionPeriodNotAligned,
            BAD_REQUEST,
            RuleDeductionsDateRangeInvalidError.withPaths(
              List(
                "/periodData/0/deductionFromDate",
                "/periodData/0/deductionToDate",
                "/periodData/1/deductionFromDate",
                "/periodData/1/deductionToDate"
              )
            )
          ),
          (
            "AA123456A",
            requestJsonErrorDuplicateDeductionPeriods,
            BAD_REQUEST,
            RuleDuplicatePeriodError.forDuplicatedPeriod(
              DateRange(LocalDate.parse("2019-06-06"), LocalDate.parse("2019-07-05")),
              List("/periodData/0", "/periodData/1")
            )
          ),
          (
            "AA123456A",
            requestJsonErrorDeductionAmountTooHigh,
            BAD_REQUEST,
            RuleDeductionAmountError.withPath("/periodData/0/deductionAmount")
          ),
          (
            "AA123456A",
            requestJsonErrorCostOfMaterialsNegative,
            BAD_REQUEST,
            RuleCostOfMaterialsError.withPath("/periodData/1/costOfMaterials")
          ),
          (
            "AA123456A",
            requestJsonErrorGrossAmountPaidNegative,
            BAD_REQUEST,
            RuleGrossAmountError.withPath("/periodData/0/grossAmountPaid")
          )
        )

        input.foreach(validationErrorTest.tupled)
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a code $downstreamCode error and status $downstreamStatus" in new NonTysTest {
            override def setupStubs(): Unit = DownstreamStub.onError(
              method = DownstreamStub.POST,
              uri = downstreamUri,
              errorStatus = downstreamStatus,
              errorBody = errorBody(downstreamCode)
            )

            val response: WSResponse = await(request().post(requestJson))
            response.status shouldBe expectedStatus
            response.json shouldBe expectedBody.asJson
            response.header("X-CorrelationId").nonEmpty shouldBe true
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val errors: Seq[(Int, String, Int, MtdError)] = List(
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_EMPREF", BAD_REQUEST, EmployerRefFormatError),
          (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_TAX_YEAR_ALIGN", BAD_REQUEST, RuleUnalignedDeductionsPeriodError),
          (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_DATE_RANGE", BAD_REQUEST, RuleDeductionsDateRangeInvalidError),
          (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_BEFORE_TAX_YEAR", BAD_REQUEST, RuleTaxYearNotEndedError),
          (CONFLICT, "CONFLICT", BAD_REQUEST, RuleDuplicateSubmissionError),
          (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_DUPLICATE_MONTH", BAD_REQUEST, RuleDuplicatePeriodError)
        )

        val extraTysErrors: Seq[(Int, String, Int, MtdError)] = List(
          (BAD_REQUEST, "INVALID_TAX_YEAR", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (UNPROCESSABLE_ENTITY, "INVALID_TAX_YEAR_ALIGN", BAD_REQUEST, RuleUnalignedDeductionsPeriodError),
          (UNPROCESSABLE_ENTITY, "EARLY_SUBMISSION", BAD_REQUEST, RuleTaxYearNotEndedError),
          (UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE", BAD_REQUEST, RuleDeductionsDateRangeInvalidError),
          (UNPROCESSABLE_ENTITY, "DUPLICATE_MONTH", BAD_REQUEST, RuleDuplicatePeriodError),
          (UNPROCESSABLE_ENTITY, "OUTSIDE_AMENDMENT_WINDOW", BAD_REQUEST, RuleOutsideAmendmentWindowError)
        )

        (errors ++ extraTysErrors).foreach(serviceErrorTest.tupled)
      }
    }
  }

  private trait Test {
    val nino: String = "AA123456A"

    val downstreamUri: String

    def setupStubs(): Unit = ()

    def request(): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/$nino/amendments")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

  }

  private trait NonTysTest extends Test {
    val downstreamUri: String = s"/income-tax/cis/deductions/$nino"
  }

  private trait TysTest extends Test {
    val downstreamUri: String = s"/income-tax/23-24/cis/deductions/$nino"
  }

}
