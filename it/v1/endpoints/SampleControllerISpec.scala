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
import v1.models.requestData.DesTaxYear
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class SampleControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"
    val taxYear = "2017-18"
    val calcId = "041f7e4d-87b9-4d4a-a296-3cfbdf92f7e2"
    val correlationId = "X-123"

    val requestJson: JsValue = Json.parse(
      s"""
         |{
         |"data": "someData"
         |}
    """.stripMargin)

    def setupStubs(): StubMapping

    def uri: String

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    val responseBody: JsValue = Json.parse(s"""
                                              |{
                                              |  "responseData" : "someResponse",
                                              |  "links": [
                                              |   {
                                              |     "href": "/mtd/template/$nino/sample-endpoint",
                                              |     "method": "GET",
                                              |     "rel": "sample-rel"
                                              |   }
                                              |  ]
                                              |}
                                              |""".stripMargin)

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin
  }

  "Calling the sample endpoint" should {

    trait SampleTest extends Test {
      def uri: String = s"/$nino/$taxYear/sampleEndpoint"
    }

    "return a 201 status code" when {

      "any valid request is made" in new SampleTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.serviceSuccess(nino, DesTaxYear.fromMtd(taxYear).toString)
        }

        val response: WSResponse = await(request().post(requestJson))
        response.status shouldBe Status.CREATED
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return bad request error" when {
      "badly formed json body" in new SampleTest {
        private val json =
          s"""
             |{
             |  badJson
             |}
    """.stripMargin

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.serviceSuccess(nino, DesTaxYear.fromMtd(taxYear).toString)
        }

        val response: WSResponse = await(request().addHttpHeaders(("Content-Type", "application/json")).post(json))
        response.status shouldBe Status.BAD_REQUEST
        response.json shouldBe Json.toJson(BadRequestError)
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new SampleTest {

            override val nino: String = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().post(requestJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "2017-18", Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20177", Status.BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2015-16", Status.BAD_REQUEST, RuleTaxYearNotSupportedError))


        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new SampleTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.serviceError(nino, DesTaxYear.fromMtd(taxYear).toString, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request().post(requestJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.BAD_REQUEST, "INVALID_REQUEST", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "NOT_FOUND", Status.NOT_FOUND, NotFoundError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}
