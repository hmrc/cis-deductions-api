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
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}


class CreateControllerSpec extends IntegrationBaseSpec {

  private trait Test {
    val nino = "AA123456A"
    val taxYear = "2017-18"
    val data = "someData"
    val correlationId = "X-123"

    val requestJson: String =
      s"""
         |{
         |  "fromDate": "2019-04-06" ,
         |  "toDate": "2020-04-05",
         |  "contractorName": "Bovis",
         |  "employerRef": "BV40092",
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
      buildRequest(s"/deductions/cis/$nino/amendments")
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the create endpoint" should {

    trait CreateTest extends Test {
      def uri: String = s"/$nino/amendments"

      def desUri: String = s"/dummy/endpoint/$nino/amendments"
    }

    "return a 200 status code" when {

      "any valid request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.deductionsServiceSuccess(nino)
        }

        val response: WSResponse = await(request().post(Json.parse(requestJson)))
        response.status shouldBe Status.OK
      }
    }
    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, body: JsValue, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new CreateTest {

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
        val requestBodyJson = Json.parse(
          """
            |{
            |  "fromDate": "2019-04-06" ,
            |  "toDate": "2020-04-05",
            |  "contractorName": "Bovis",
            |  "employerRef": "BV40092",
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
        """.stripMargin
        )
        val requestBodyJsonErrorFromDate = Json.parse(
          """
            |{
            |  "fromDate": "04-06-2019" ,
            |  "toDate": "2020-04-05",
            |  "contractorName": "Bovis",
            |  "employerRef": "BV40092",
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
        """.stripMargin
        )
        val requestBodyJsonErrorToDate = Json.parse(
          """
            |{
            |  "fromDate": "2019-04-06" ,
            |  "toDate": "04-05-2020",
            |  "contractorName": "Bovis",
            |  "employerRef": "BV40092",
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
        """.stripMargin
        )
        val requestBodyJsonErrorDeductionFromDate = Json.parse(
          """
            |{
            |  "fromDate": "2019-04-06" ,
            |  "toDate": "2020-04-05",
            |  "contractorName": "Bovis",
            |  "employerRef": "BV40092",
            |  "periodData": [
            |      {
            |      "deductionAmount": 355.00,
            |      "deductionFromDate": "06-06-2019",
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
        """.stripMargin
        )
        val requestBodyJsonErrorDeductionToDate = Json.parse(
          """
            |{
            |  "fromDate": "2019-04-06" ,
            |  "toDate": "2020-04-05",
            |  "contractorName": "Bovis",
            |  "employerRef": "BV40092",
            |  "periodData": [
            |      {
            |      "deductionAmount": 355.00,
            |      "deductionFromDate": "2019-06-06",
            |      "deductionToDate": "07-05-2019",
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
        """.stripMargin
        )
        val input = Seq(
          ("AA1123A", requestBodyJson, Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", requestBodyJsonErrorFromDate, Status.BAD_REQUEST, FromDateFormatError),
          ("AA123456A", requestBodyJsonErrorToDate, Status.BAD_REQUEST, ToDateFormatError),
          ("AA123456A", requestBodyJsonErrorDeductionToDate, Status.BAD_REQUEST, DeductionToDateFormatError),
          ("AA123456A", requestBodyJsonErrorDeductionFromDate, Status.BAD_REQUEST, DeductionFromDateFormatError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }
    }
  }
}