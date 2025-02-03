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

package v3.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.errors.{RuleOutsideAmendmentWindowError, SubmissionIdFormatError}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import shared.support.IntegrationBaseSpec
import v3.fixtures.CreateRequestFixtures._

class DeleteControllerISpec extends IntegrationBaseSpec {

  "Calling the delete endpoint" should {

    "return a 204 status code" when {

      "a valid request is made" in new NonTysTest {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)

          DownstreamStub
            .when(method = DownstreamStub.DELETE, uri = downstreamUri)
            .thenReturn(status = NO_CONTENT, JsObject.empty)
        }
        val response: WSResponse = await(mtdRequest().delete())
        response.status shouldBe NO_CONTENT
      }
    }

    "a valid request is made for a Tax Year Specific tax year" in new TysIfsTest {
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        MtdIdLookupStub.ninoFound(nino)

        DownstreamStub
          .when(method = DownstreamStub.DELETE, uri = downstreamUri)
          .thenReturn(status = NO_CONTENT, JsObject.empty)
      }
      val response: WSResponse = await(mtdRequest().delete())
      response.status shouldBe NO_CONTENT
    }

    "return error according to spec" when {
      case class RequestData(nino: String, submissionId: String, taxYear: Option[String])

      "validation error" when {

        def validationErrorTest(requestData: RequestData, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest {
            override val nino: String                 = requestData.nino
            override val submissionId: String         = requestData.submissionId
            override val maybeTaxYear: Option[String] = requestData.taxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(mtdRequest().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (RequestData("AA1123A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", None), BAD_REQUEST, NinoFormatError),
          (RequestData("AA123456A", "4cc-81f5-e6acd1099f3c", None), BAD_REQUEST, SubmissionIdFormatError)
        )

        val extraTysInput = Seq(
          (RequestData("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Some("2023")), BAD_REQUEST, TaxYearFormatError),
          (RequestData("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Some("2021-22")), BAD_REQUEST, InvalidTaxYearParameterError)
        )

        (input ++ extraTysInput).foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)

              DownstreamStub
                .when(method = DownstreamStub.DELETE, uri = downstreamUri)
                .thenReturn(status = downstreamStatus, Json.parse(errorBody(downstreamCode)))
            }

            val response: WSResponse = await(mtdRequest().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_SUBMISSION_ID", BAD_REQUEST, SubmissionIdFormatError),
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (UNPROCESSABLE_ENTITY, "OUTSIDE_AMENDMENT_WINDOW", BAD_REQUEST, RuleOutsideAmendmentWindowError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {
    val nino         = "AA123456A"
    val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
    def maybeTaxYear: Option[String]

    def downstreamUri: String

    private def taxYearParam = maybeTaxYear.map(ty => s"?taxYear=$ty").getOrElse("")

    def setupStubs(): StubMapping

    def mtdRequest(): WSRequest = {
      setupStubs()
      buildRequest(s"/$nino/amendments/$submissionId$taxYearParam")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  private trait NonTysTest extends Test {
    def maybeTaxYear: Option[String] = None
    def downstreamUri: String        = s"/income-tax/cis/deductions/$nino/submissionId/$submissionId"
  }

  private trait TysIfsTest extends Test {
    def maybeTaxYear: Option[String] = Option("2023-24")
    def downstreamTaxYear: String    = "23-24"
    def downstreamUri: String        = s"/income-tax/cis/deductions/$downstreamTaxYear/$nino/submissionId/$submissionId"
  }

}
