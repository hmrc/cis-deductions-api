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
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.fixtures.CreateRequestFixtures._
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class DeleteControllerISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino = "AA123456A"
    val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    def uri: String = s"/$nino/amendments/$submissionId"
    def desUri: String = s"/income-tax/cis/deductions/$nino/submissionId/$submissionId"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the delete endpoint" should {

    "return a 204 status code" when {

      "any valid request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.mockDes(DesStub.DELETE, desUri, Status.NO_CONTENT, Json.obj(), None)
        }
        val response: WSResponse = await(request().delete())
        response.status shouldBe Status.NO_CONTENT
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestId: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val submissionId: String = requestId

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }
        val input = Seq(
          ("AA1123A", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "4cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, SubmissionIdFormatError)
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
              DesStub.mockDes(DesStub.DELETE, desUri, desStatus, Json.parse(errorBody(desCode)), None)
            }

            val response: WSResponse = await(request().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.INTERNAL_SERVER_ERROR, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INVALID_SUBMISSION_ID", Status.BAD_REQUEST, SubmissionIdFormatError),
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}