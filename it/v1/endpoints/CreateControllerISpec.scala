/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.fixtures.CreateRequestFixtures._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class CreateControllerISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino = "AA123456A"

    def uri: String = s"/$nino/amendments"
    def desUri: String = s"/income-tax/cis/deductions/$nino"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the create endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.mockDes(DesStub.POST, desUri, Status.OK, deductionsResponseBody, None)
        }
        val response: WSResponse = await(request().post(requestBodyJson))
        response.status shouldBe Status.OK
        response.json shouldBe deductionsResponseBody
      }
    }
    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, body: JsValue, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().post(body))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", requestBodyJson, Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", emptyRequest, Status.BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A", requestInvalidEmpRef, Status.BAD_REQUEST, EmployerRefFormatError),
          ("AA123456A", requestRuleDeductionAmountJson, Status.BAD_REQUEST, RuleDeductionAmountError),
          ("AA123456A", requestInvalidRuleCostOfMaterialsJson, Status.BAD_REQUEST, RuleCostOfMaterialsError),
          ("AA123456A", requestInvalidGrossAmountJson, Status.BAD_REQUEST, RuleGrossAmountError),
          ("AA123456A", requestInvalidDateRangeJson, Status.FORBIDDEN, RuleDateRangeInvalidError),
          ("AA123456A", requestBodyJsonErrorFromDate, Status.BAD_REQUEST, FromDateFormatError),
          ("AA123456A", requestBodyJsonErrorToDate, Status.BAD_REQUEST, ToDateFormatError),
          ("AA123456A", requestBodyJsonErrorDeductionToDate, Status.BAD_REQUEST, DeductionToDateFormatError),
          ("AA123456A", requestBodyJsonErrorDeductionFromDate, Status.BAD_REQUEST, DeductionFromDateFormatError),
          ("AA123456A", requestBodyJsonErrorTaxYearNotSupported, Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", requestBodyJsonFromDate13MonthsBeforeToDate, Status.FORBIDDEN, RuleDeductionsDateRangeInvalidError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new Test {
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.mockDes(DesStub.POST, desUri, desStatus, Json.parse(errorBody(desCode)), None)
            }

            val response: WSResponse = await(request().post(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_PAYLOAD", Status.BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          (Status.BAD_REQUEST, "INVALID_EMPREF", Status.BAD_REQUEST, EmployerRefFormatError),
          (Status.UNPROCESSABLE_ENTITY, "INVALID_REQUEST_TAX_YEAR_ALIGN", Status.FORBIDDEN, RuleUnalignedDeductionsPeriodError),
          (Status.UNPROCESSABLE_ENTITY, "INVALID_REQUEST_DATE_RANGE", Status.FORBIDDEN, RuleDeductionsDateRangeInvalidError),
          (Status.UNPROCESSABLE_ENTITY, "INVALID_REQUEST_BEFORE_TAX_YEAR", Status.FORBIDDEN, RuleTaxYearNotEndedError),
          (Status.CONFLICT, "CONFLICT", Status.FORBIDDEN, RuleDuplicateSubmissionError),
          (Status.UNPROCESSABLE_ENTITY, "INVALID_REQUEST_DUPLICATE_MONTH", Status.FORBIDDEN, RuleDuplicatePeriodError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}