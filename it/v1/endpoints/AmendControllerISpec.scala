/*
 * Copyright 2022 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import data.AmendDataExamples._
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class AmendControllerISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino         = "AA123456A"
    val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    def uri: String    = s"/$nino/amendments/$submissionId"
    def desUri: String = s"/income-tax/cis/deductions/$nino/submissionId/$submissionId"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }
  }

  "Calling the amend endpoint" should {

    "return a 204 status code" when {

      "any valid request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.mockDes(DownstreamStub.PUT, desUri, Status.NO_CONTENT, Json.obj(), None)
        }
        val response: WSResponse = await(request().put(Json.parse(requestJson)))
        response.status shouldBe Status.NO_CONTENT
      }
    }
    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestId: String, body: JsValue, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String         = requestNino
            override val submissionId: String = requestId

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(body))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "ID-SUB", requestBodyJson, Status.BAD_REQUEST, SubmissionIdFormatError),
          ("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Json.parse("""{}"""), Status.BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJsonErrorDeductionToDate, Status.BAD_REQUEST, DeductionToDateFormatError),
          (
            "AA123456A",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            requestBodyJsonErrorDeductionFromDate,
            Status.BAD_REQUEST,
            DeductionFromDateFormatError),
          ("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJsonErrorRuleCostOfMaterial, Status.BAD_REQUEST, RuleCostOfMaterialsError),
          ("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJsonErrorRuleGrossAmountPaid, Status.BAD_REQUEST, RuleGrossAmountError),
          ("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJsonErrorRuleDeductionAmount, Status.BAD_REQUEST, RuleDeductionAmountError)
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
              DownstreamStub.mockDes(DownstreamStub.PUT, desUri, desStatus, Json.parse(errorBody(desCode)), None)
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (Status.BAD_REQUEST, "INVALID_PAYLOAD", Status.BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_SUBMISSION_ID", Status.BAD_REQUEST, SubmissionIdFormatError),
          (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (Status.UNPROCESSABLE_ENTITY, "INVALID_TAX_YEAR_ALIGN", Status.FORBIDDEN, RuleUnalignedDeductionsPeriodError),
          (Status.UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE", Status.FORBIDDEN, RuleDeductionsDateRangeInvalidError),
          (Status.UNPROCESSABLE_ENTITY, "DUPLICATE_MONTH", Status.FORBIDDEN, RuleDuplicatePeriodError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
