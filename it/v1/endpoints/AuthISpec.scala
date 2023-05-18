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

import api.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.fixtures.CreateRequestFixtures._

class AuthISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino          = "AA123456A"
    val taxYear       = "2017-18"
    val data          = "someData"
    val correlationId = "X-123"

    def desUri: String = s"/income-tax/cis/deductions/$nino"

    val requestJson: String =
      s"""
        |{
        |  "fromDate": "2019-04-06" ,
        |  "toDate": "2020-04-05",
        |  "contractorName": "Bovis",
        |  "employerRef": "123/AB56797",
        |  "periodData": [
        |      {
        |      "deductionAmount": 355.00,
        |      "deductionFromDate": "2019-06-06",
        |      "deductionToDate": "2019-07-05",
        |      "costOfMaterials": 35.00,
        |      "grossAmountPaid": 1457.00
        |    },
        |    {
        |      "deductionAmount": 355.00,
        |      "deductionFromDate": "2019-07-06",
        |      "deductionToDate": "2019-08-05",
        |      "costOfMaterials": 35.00,
        |      "grossAmountPaid": 1457.00
        |    }
        |  ]
        |}
        |""".stripMargin

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/$nino/amendments")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  "Calling the sample endpoint" when {

    "the NINO cannot be converted to a MTD ID" should {

      "return 500" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.internalServerError(nino)
        }

        val response: WSResponse = await(request().post(Json.parse(requestJson)))
        response.status shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is authorised" should {

      "return 200" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.mockDownstream(DownstreamStub.POST, desUri, Status.OK, deductionsResponseBody, None)
        }

        val response: WSResponse = await(request().post(Json.parse(requestJson)))
        response.status shouldBe Status.OK
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT logged in" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedNotLoggedIn()
        }

        val response: WSResponse = await(request().post(Json.parse(requestJson)))
        response.status shouldBe Status.FORBIDDEN
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT authorised" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedOther()
        }

        val response: WSResponse = await(request().post(Json.parse(requestJson)))
        response.status shouldBe Status.FORBIDDEN
      }
    }
  }

}
