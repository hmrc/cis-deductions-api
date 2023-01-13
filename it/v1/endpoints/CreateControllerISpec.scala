/*
 * Copyright 2023 HM Revenue & Customs
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

package v1.endpoints

import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.fixtures.CreateRequestFixtures._
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class CreateControllerISpec extends IntegrationBaseSpec {

  "Calling the create endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new NonTysTest {
        override def setupStubs(): Unit = {
          DownstreamStub.mockDownstream(DownstreamStub.POST, downstreamUri, OK, deductionsResponseBody, None)
        }
        val response: WSResponse = await(request().post(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe deductionsResponseBody
      }

      "any valid request is made for a Tax Year Specific (TYS) tax year" in new TysIfsTest {
        override def setupStubs(): Unit = {
          DownstreamStub.mockDownstream(DownstreamStub.POST, downstreamUri, CREATED, deductionsResponseBody, None)
        }

        val response: WSResponse = await(request().post(requestBodyJsonTys))
        response.json shouldBe deductionsResponseBodyTys
        response.status shouldBe OK
      }
    }
    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, body: JsValue, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest {

            override val nino: String = requestNino

            val response: WSResponse = await(request().post(body))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          ("AA1123A", requestBodyJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", emptyRequest, BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A", requestInvalidEmpRef, BAD_REQUEST, EmployerRefFormatError),
          ("AA123456A", requestRuleDeductionAmountJson, BAD_REQUEST, RuleDeductionAmountError),
          ("AA123456A", requestInvalidRuleCostOfMaterialsJson, BAD_REQUEST, RuleCostOfMaterialsError),
          ("AA123456A", requestInvalidGrossAmountJson, BAD_REQUEST, RuleGrossAmountError),
          ("AA123456A", requestInvalidDateRangeJson, BAD_REQUEST, RuleDateRangeInvalidError),
          ("AA123456A", requestBodyJsonErrorFromDate, BAD_REQUEST, FromDateFormatError),
          ("AA123456A", requestBodyJsonErrorToDate, BAD_REQUEST, ToDateFormatError),
          ("AA123456A", requestBodyJsonErrorDeductionToDate, BAD_REQUEST, DeductionToDateFormatError),
          ("AA123456A", requestBodyJsonErrorDeductionFromDate, BAD_REQUEST, DeductionFromDateFormatError),
          ("AA123456A", requestBodyJsonErrorTaxYearNotSupported, BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", requestBodyJsonFromDate13MonthsBeforeToDate, BAD_REQUEST, RuleDeductionsDateRangeInvalidError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {
            override def setupStubs(): Unit = {
              DownstreamStub.mockDownstream(DownstreamStub.POST, downstreamUri, downstreamStatus, Json.parse(errorBody(downstreamCode)), None)
            }

            val response: WSResponse = await(request().post(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = List(
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_PAYLOAD", BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          (BAD_REQUEST, "INVALID_EMPREF", BAD_REQUEST, EmployerRefFormatError),
          (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_TAX_YEAR_ALIGN", BAD_REQUEST, RuleUnalignedDeductionsPeriodError),
          (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_DATE_RANGE", BAD_REQUEST, RuleDeductionsDateRangeInvalidError),
          (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_BEFORE_TAX_YEAR", BAD_REQUEST, RuleTaxYearNotEndedError),
          (CONFLICT, "CONFLICT", BAD_REQUEST, RuleDuplicateSubmissionError),
          (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_DUPLICATE_MONTH", BAD_REQUEST, RuleDuplicatePeriodError)
        )

        val extraTysErrors = List(
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {
    val nino = "AA123456A"

    val downstreamUri: String

    def setupStubs(): Unit = {}

    def request(): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()

      buildRequest(s"/$nino/amendments")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  private trait NonTysTest extends Test {
    val downstreamUri: String = s"/income-tax/cis/deductions/$nino"
  }

  private trait TysIfsTest extends Test {
    val downstreamUri: String = s"/income-tax/23-24/cis/deductions/$nino"

    override def request(): WSRequest =
      super.request().addHttpHeaders("suspend-temporal-validations" -> "true")

  }

}
