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
        response.json shouldBe singleDeductionJsonHateoas(fromDate, toDate)
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
        response.json shouldBe singleDeductionJsonHateoas(fromDate, toDate, "?taxYear=2023-24")

      }

      "return error according to spec" when {

        def validationErrorTest(requestNino: String,
                                requestFromDate: String,
                                requestToDate: String,
                                requestSource: String,
                                expectedStatus: Int,
                                expectedBody: MtdError,
                                qParams: Option[Seq[(String, String)]]): Unit = {

          s"validation fails with ${expectedBody.code} error" in new NonTysTest {
            override def fromDate: String = requestFromDate
            override def toDate: String   = requestToDate

            override def mtdQueryParams: Seq[(String, String)] = qParams.getOrElse(super.mtdQueryParams)

            override val nino: String   = requestNino
            override val source: String = requestSource

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
          ("AA12345", "2020-04-06", "2021-04-05", "customer", BAD_REQUEST, NinoFormatError, None),
          ("AA123456B", "2020-04", "2021-04-05", "customer", BAD_REQUEST, FromDateFormatError, None),
          ("AA123456B", "2020-04-06", "2021-04", "customer", BAD_REQUEST, ToDateFormatError, None),
          ("AA123456B", "2020-04-06", "2021-04-05", "asdf", BAD_REQUEST, RuleSourceError, None),
          ("AA123456B", "2022-04-05", "2021-04-06", "customer", BAD_REQUEST, RuleDateRangeInvalidError, None),
          (
            "AA123456B",
            "2020-04-06",
            "2021-04-05",
            "customer",
            BAD_REQUEST,
            RuleMissingToDateError,
            Some(Seq("fromDate" -> "2020-04-06", "source" -> "all"))),
          (
            "AA123456B",
            "2020-04-06",
            "2021-04-05",
            "customer",
            BAD_REQUEST,
            RuleMissingFromDateError,
            Some(Seq("toDate" -> "2021-04-05", "source" -> "all")))
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
        (BAD_REQUEST, "INVALID_PERIOD_START", BAD_REQUEST, FromDateFormatError),
        (BAD_REQUEST, "INVALID_PERIOD_END", BAD_REQUEST, ToDateFormatError),
        (UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE", BAD_REQUEST, RuleDateRangeOutOfDate)
      )
      errors.foreach(args => (serviceErrorTest _).tupled(args))

      val extraTysErrors = List(
        (BAD_REQUEST, "INVALID_TAX_YEAR", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_DATE_RANGE", BAD_REQUEST, RuleTaxYearRangeInvalidError),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
      )
      extraTysErrors.foreach(args => (tysServiceErrorTest _).tupled(args))
    }
  }

  private trait Test {
    def fromDate: String
    def toDate: String

    val nino   = "AA123456A"
    val source = "customer"

    def mtdQueryParams: Seq[(String, String)] = List("fromDate" -> fromDate, "toDate" -> toDate, "source" -> source)
    def downstreamQueryParams: Seq[(String, String)]

    def setupStubs(): StubMapping

    def mtdRequest(): WSRequest = {
      setupStubs()
      buildRequest(s"/$nino/current-position")
        .withQueryStringParameters(mtdQueryParams: _*)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  private trait NonTysTest extends Test {
    def fromDate              = "2019-04-06"
    def toDate                = "2020-04-05"
    def downstreamQueryParams = List("periodStart" -> fromDate, "periodEnd" -> toDate, "source" -> source)
    def downstreamUri: String = s"/income-tax/cis/deductions/$nino"
  }

  private trait TysIfsTest extends Test {
    def fromDate                  = "2023-04-06"
    def toDate                    = "2024-04-05"
    def downstreamTaxYear: String = "23-24"
    def downstreamQueryParams     = List("startDate" -> fromDate, "endDate" -> toDate, "source" -> source)
    def downstreamUri: String     = s"/income-tax/cis/deductions/$downstreamTaxYear/$nino"
  }

}
