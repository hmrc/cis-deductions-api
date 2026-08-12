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

package v3.amend

import api.models.domain.DateRange
import api.models.errors.*
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import api.support.IntegrationBaseSpec
import models.errors.*
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import v3.fixtures.AmendRequestFixtures.*
import v3.models.errors.CisDeductionsApiCommonErrors.{DeductionFromDateFormatError, DeductionToDateFormatError}

import java.time.LocalDate

class AmendControllerISpec extends IntegrationBaseSpec {

  "Calling the 'Amend CIS Deductions for Subcontractor' endpoint" should {
    "return a 204 status code" when {
      "any valid non-TYS request is made" in new NonTysTest {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = DownstreamStub.PUT,
          uri = downstreamUri,
          status = NO_CONTENT,
          body = JsObject.empty
        )

        val response: WSResponse = await(request().put(requestJson))
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
        response.header("Content-Type") shouldBe None
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "any valid TYS request is made" in new TysTest {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = DownstreamStub.PUT,
          uri = downstreamUri,
          status = NO_CONTENT,
          body = JsObject.empty
        )

        val response: WSResponse = await(request().put(requestBodyJsonTys))
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
        response.header("Content-Type") shouldBe None
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String, requestId: String, body: JsValue, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest {
            override val nino: String         = requestNino
            override val submissionId: String = requestId

            val response: WSResponse = await(request().put(body))
            response.status shouldBe expectedStatus
            response.json shouldBe expectedBody.asJson
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input: Seq[(String, String, JsValue, Int, MtdError)] = List(
          ("AA1123A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "4557ecb5", requestJson, BAD_REQUEST, SubmissionIdFormatError),
          ("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestJsonErrorTaxYearNotSupported, BAD_REQUEST, RuleTaxYearNotSupportedError),
          (
            "AA123456A",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            requestJsonErrorDeductionFromDate,
            BAD_REQUEST,
            DeductionFromDateFormatError.withPaths(List("/periodData/0/deductionFromDate", "/periodData/1/deductionFromDate"))
          ),
          (
            "AA123456A",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            requestJsonErrorDeductionToDate,
            BAD_REQUEST,
            DeductionToDateFormatError.withPaths(List("/periodData/0/deductionToDate", "/periodData/1/deductionToDate"))
          ),
          (
            "AA123456A",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
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
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            requestJsonErrorDuplicateDeductionPeriods,
            BAD_REQUEST,
            RuleDuplicatePeriodError.forDuplicatedPeriod(
              DateRange(LocalDate.parse("2019-06-06"), LocalDate.parse("2019-07-05")),
              List("/periodData/0", "/periodData/1")
            )
          ),
          (
            "AA123456A",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            requestJsonErrorDeductionAmountTooHigh,
            BAD_REQUEST,
            RuleDeductionAmountError.withPath("/periodData/0/deductionAmount")
          ),
          (
            "AA123456A",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            requestJsonErrorCostOfMaterialsNegative,
            BAD_REQUEST,
            RuleCostOfMaterialsError.withPath("/periodData/1/costOfMaterials")
          ),
          (
            "AA123456A",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
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
              method = DownstreamStub.PUT,
              uri = downstreamUri,
              errorStatus = downstreamStatus,
              errorBody = errorBody(downstreamCode)
            )

            val response: WSResponse = await(request().put(requestJson))
            response.status shouldBe expectedStatus
            response.json shouldBe expectedBody.asJson
            response.header("X-CorrelationId").nonEmpty shouldBe true
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val errors: Seq[(Int, String, Int, MtdError)] = List(
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_SUBMISSION_ID", BAD_REQUEST, SubmissionIdFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "INVALID_TAX_YEAR_ALIGN", BAD_REQUEST, RuleUnalignedDeductionsPeriodError),
          (UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE", BAD_REQUEST, RuleDeductionsDateRangeInvalidError),
          (UNPROCESSABLE_ENTITY, "DUPLICATE_MONTH", BAD_REQUEST, RuleDuplicatePeriodError)
        )

        val extraTysErrors: Seq[(Int, String, Int, MtdError)] = List(
          (BAD_REQUEST, "INVALID_TAX_YEAR", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (UNPROCESSABLE_ENTITY, "OUTSIDE_AMENDMENT_WINDOW", BAD_REQUEST, RuleOutsideAmendmentWindowError)
        )

        (errors ++ extraTysErrors).foreach(serviceErrorTest.tupled)
      }
    }
  }

  private trait Test {
    val nino: String         = "AA123456A"
    val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val downstreamUri: String

    def setupStubs(): Unit = ()

    def request(): WSRequest = {
      AuthStub.authorised()
      AuditStub.audit()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/$nino/amendments/$submissionId")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
        |{
        |  "failures": [
        |    {
        |      "code": "$code",
        |      "reason": "downstream message"
        |    }
        |  ]
        |}
      """.stripMargin

  }

  private trait NonTysTest extends Test {
    val downstreamUri: String = s"/income-tax/cis/deductions/$nino/submissionId/$submissionId"
  }

  private trait TysTest extends Test {
    val downstreamUri: String = s"/income-tax/23-24/cis/deductions/$nino/$submissionId"
  }

}
