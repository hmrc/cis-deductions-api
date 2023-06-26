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

package v2.endpoints

import api.models.errors._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v2.fixtures.RetrieveJson._
import api.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class RetrieveControllerISpec extends IntegrationBaseSpec {

  "Calling the retrieve endpoint" should {

    "return an OK response" when {

      "a valid request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)

          DownstreamStub
            .when(method = DownstreamStub.GET, uri = downstreamUri, queryParams = downstreamQueryParams.toMap)
            .thenReturn(status = OK, singleDeductionJson(fromDate, toDate))
        }

        val response: WSResponse = await(mtdRequest().get())

        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe singleDeductionJsonHateoas(fromDate, toDate, taxYear)
      }

      "valid request is made without any IDs" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)

          DownstreamStub
            .when(method = DownstreamStub.GET, uri = downstreamUri, queryParams = downstreamQueryParams.toMap)
            .thenReturn(status = OK, singleDeductionWithoutIdsJson)
        }

        val response: WSResponse = await(mtdRequest().get())

        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe singleDeductionWithoutIdsJsonHateoas
      }

      "a valid request is made for a Tax Year Specific tax year" in new TysIfsTest {

        override def fromDate: String = "2023-04-06"
        override def toDate: String   = "2024-04-05"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)

          DownstreamStub
            .when(method = DownstreamStub.GET, uri = downstreamUri, queryParams = downstreamQueryParams.toMap)
            .thenReturn(status = OK, singleDeductionJson(fromDate, toDate))
        }

        val response: WSResponse = await(mtdRequest().get())
        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe singleDeductionJsonHateoas(fromDate, toDate, "2023-24")

      }

      "return error according to spec" when {

        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestSource: String,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {

          s"validation fails with ${expectedBody.code} error" in new NonTysTest {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear
            override val source: String  = requestSource

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(mtdRequest().get())
            response.status shouldBe expectedStatus
            response.json shouldBe expectedBody.asJson
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA12345", "2020-21", "customer", BAD_REQUEST, NinoFormatError),
          ("AA123456B", "2021-23", "customer", BAD_REQUEST, RuleDateRangeInvalidError),
          // TaxYearNotSupported
          ("AA123456B", "2020-21", "asdf", BAD_REQUEST, RuleSourceError),
          ("AA123456B", "2021--22", "customer", BAD_REQUEST, TaxYearFormatError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }
    }

    "downstream service error" when {

      def errorBody(code: String): JsValue =
        Json.parse(s"""{
             |  "code": "$code",
             |  "reason": "downstream error message"
             |}""".stripMargin)

      def serviceErrorTest(downstreamStatus: Int, downstreamErrorCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns $downstreamErrorCode with status $downstreamStatus" in new NonTysTest {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.mockDownstream(DownstreamStub.GET, downstreamUri, downstreamStatus, errorBody(downstreamErrorCode), None)
          }

          val response: WSResponse = await(mtdRequest().get())
          response.json shouldBe expectedBody.asJson
          response.status shouldBe expectedStatus
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      def tysServiceErrorTest(downstreamStatus: Int, downstreamErrorCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"TYS downstream returns $downstreamErrorCode with status $downstreamStatus" in new TysIfsTest {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.mockDownstream(DownstreamStub.GET, downstreamUri, downstreamStatus, errorBody(downstreamErrorCode), None)
          }

          val response: WSResponse = await(mtdRequest().get())
          response.json shouldBe expectedBody.asJson
          response.status shouldBe expectedStatus
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val errors = List(
        (BAD_REQUEST, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_REQUEST", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_PERIOD_START", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_PERIOD_END", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE", BAD_REQUEST, RuleDateRangeOutOfDate)
      )
      errors.foreach(args => (serviceErrorTest _).tupled(args))

      val extraTysErrors = List(
        (BAD_REQUEST, "INVALID_TAX_YEAR", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_START_DATE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_END_DATE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_DATE_RANGE", BAD_REQUEST, RuleTaxYearRangeInvalidError),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_ALIGNED", BAD_REQUEST, RuleTaxYearNotAligned),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
      )
      extraTysErrors.foreach(args => (tysServiceErrorTest _).tupled(args))
    }
  }

  private trait Test {
    def fromDate: String
    def toDate: String

    val nino    = "AA123456A"
    val taxYear = "2021-22"
    val source  = "customer"

    def downstreamQueryParams: Seq[(String, String)]

    def setupStubs(): StubMapping

    def mtdRequest(): WSRequest = {
      setupStubs()
      buildRequest(s"/$nino/current-position/$taxYear/$source")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  private trait NonTysTest extends Test {
    def fromDate: String                             = "2019-04-06"
    def toDate: String                               = "2020-04-05"
    def downstreamQueryParams: Seq[(String, String)] = List("periodStart" -> fromDate, "periodEnd" -> toDate, "source" -> source)
    def downstreamUri: String                        = s"/income-tax/cis/deductions/$nino"
  }

  private trait TysIfsTest extends Test {
    def fromDate: String                             = "2023-04-06"
    def toDate: String                               = "2024-04-05"
    def downstreamTaxYear: String                    = "23-24"
    def downstreamQueryParams: Seq[(String, String)] = List("startDate" -> fromDate, "endDate" -> toDate, "source" -> source)
    def downstreamUri: String                        = s"/income-tax/cis/deductions/$downstreamTaxYear/$nino"
  }

}
