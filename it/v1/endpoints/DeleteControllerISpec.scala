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

import api.models.errors._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.fixtures.CreateRequestFixtures._
import v1.models.request.delete.DeleteRawData
import v1.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

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

      "validation error" when {

        def validationErrorTest(requestData: DeleteRawData, expectedStatus: Int, expectedBody: MtdError): Unit = {
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
          (DeleteRawData("AA1123A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", None), Status.BAD_REQUEST, NinoFormatError),
          (DeleteRawData("AA123456A", "4cc-81f5-e6acd1099f3c", None), Status.BAD_REQUEST, SubmissionIdFormatError)
        )

        val extraTysInput = Seq(
          (DeleteRawData("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Some("2023")), Status.BAD_REQUEST, TaxYearFormatError),
          (DeleteRawData("AA123456A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Some("2021-22")), Status.BAD_REQUEST, InvalidTaxYearParameterError)
        )

        (input ++ extraTysInput).foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new NonTysTest {
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)

              DownstreamStub
                .when(method = DownstreamStub.DELETE, uri = downstreamUri)
                .thenReturn(status = desStatus, Json.parse(errorBody(desCode)))
            }

            val response: WSResponse = await(mtdRequest().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (Status.INTERNAL_SERVER_ERROR, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (Status.BAD_REQUEST, "INVALID_SUBMISSION_ID", Status.BAD_REQUEST, SubmissionIdFormatError),
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError)
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
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
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
