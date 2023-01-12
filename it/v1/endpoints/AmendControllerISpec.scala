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

import data.AmendDataExamples._
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class AmendControllerISpec extends IntegrationBaseSpec {

  "Calling the amend endpoint" should {

    "return a 204 status code" when {

      "any valid request is made" in new NonTysTest {

        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, Json.obj())

        val response: WSResponse = await(request().put(Json.parse(requestJson)))
        response.status shouldBe NO_CONTENT

      }

      "any valid TYS request is made" in new TysIfsTest {

        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, Json.obj())

        val response: WSResponse = await(request().put(Json.parse(requestTysJson)))
        response.status shouldBe NO_CONTENT

      }
    }

    "return error according to spec" when {

      "validation error" when {

        def validationErrorTest(requestNino: String, requestId: String, body: JsValue, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest {

            override val nino: String         = requestNino
            override val submissionId: String = requestId

            override def setupStubs(): Unit = MtdIdLookupStub.ninoFound(requestNino)

            val response: WSResponse = await(request().put(body))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "ID-SUB", requestBodyJson, BAD_REQUEST, SubmissionIdFormatError),
          ("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Json.parse("""{}"""), BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJsonErrorDeductionToDate, BAD_REQUEST, DeductionToDateFormatError),
          ("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJsonErrorDeductionFromDate, BAD_REQUEST, DeductionFromDateFormatError),
          ("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJsonErrorRuleCostOfMaterial, BAD_REQUEST, RuleCostOfMaterialsError),
          ("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJsonErrorRuleGrossAmountPaid, BAD_REQUEST, RuleGrossAmountError),
          ("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", requestBodyJsonErrorRuleDeductionAmount, BAD_REQUEST, RuleDeductionAmountError)
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {

        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {

            override def setupStubs(): Unit =
              DownstreamStub.mockDownstream(DownstreamStub.PUT, downstreamUri, downstreamStatus, Json.parse(errorBody(downstreamCode)), None)

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = List(
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_SUBMISSION_ID", BAD_REQUEST, SubmissionIdFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (UNPROCESSABLE_ENTITY, "INVALID_TAX_YEAR_ALIGN", BAD_REQUEST, RuleUnalignedDeductionsPeriodError),
          (UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE", BAD_REQUEST, RuleDeductionsDateRangeInvalidError),
          (UNPROCESSABLE_ENTITY, "DUPLICATE_MONTH", BAD_REQUEST, RuleDuplicatePeriodError)
        )

        val extraTysErrors = List(
          (BAD_REQUEST, "INVALID_TAX_YEAR", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    val nino         = "AA123456A"
    val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    def mtdUri: String = s"/$nino/amendments/$submissionId"

    def downstreamUri: String

    def setupStubs(): Unit = ()

    def request(): WSRequest = {
      AuthStub.authorised()
      AuditStub.audit()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  private trait NonTysTest extends Test {

    def downstreamUri: String = s"/income-tax/cis/deductions/$nino/submissionId/$submissionId"
  }

  private trait TysIfsTest extends Test {

    def downstreamUri: String = s"/income-tax/23-24/cis/deductions/$nino/$submissionId"
  }

}
