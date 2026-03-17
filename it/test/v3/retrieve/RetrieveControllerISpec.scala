/*
 * Copyright 2026 HM Revenue & Customs
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

package v3.retrieve

import models.errors.RuleSourceInvalidError
import play.api.http.HeaderNames.*
import play.api.http.Status.*
import play.api.libs.ws.{WSRequest, WSResponse}
import shared.models.errors.*
import shared.services.*
import shared.support.IntegrationBaseSpec
import v3.fixtures.RetrieveJson.*

class RetrieveControllerISpec extends IntegrationBaseSpec {

  "Calling the retrieve endpoint" should {
    "return an OK response" when {
      "a valid request is made for a pre-TYS tax year" in new NonTysTest {
        override def setupStubs(): Unit =
          DownstreamStub
            .onSuccess(
              method = DownstreamStub.GET,
              uri = downstreamUri,
              status = OK,
              queryParams = downstreamQueryParams,
              body = singleDeductionJson(fromDate, toDate))

        val response: WSResponse = await(mtdRequest.get())

        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe singleDeductionJson(fromDate, toDate)
      }

      "a valid request is made without any IDs for a pre-TYS tax year" in new NonTysTest {
        override def setupStubs(): Unit =
          DownstreamStub
            .onSuccess(
              method = DownstreamStub.GET,
              uri = downstreamUri,
              status = OK,
              queryParams = downstreamQueryParams,
              body = singleDeductionWithoutIdsJson)

        val response: WSResponse = await(mtdRequest.get())

        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe singleDeductionWithoutIdsJson
      }

      "a valid request is made for a Tax Year Specific tax year" in new TysHipTest {
        override def setupStubs(): Unit =
          DownstreamStub
            .onSuccess(
              method = DownstreamStub.GET,
              uri = downstreamUri,
              status = OK,
              queryParams = downstreamQueryParams,
              body = singleDeductionJson(fromDate, toDate))

        val response: WSResponse = await(mtdRequest.get())
        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe singleDeductionJson(fromDate, toDate)
      }
    }

    "return error according to spec" when {
      val input = List(
        ("AA12345", "2020-21", "customer", BAD_REQUEST, NinoFormatError),
        ("AA123456B", "2021-23", "customer", BAD_REQUEST, RuleTaxYearRangeInvalidError),
        ("AA123456B", "2020-21", "asdf", BAD_REQUEST, RuleSourceInvalidError),
        ("AA123456B", "2021--22", "customer", BAD_REQUEST, TaxYearFormatError)
      )

      input.foreach { case (requestNino, requestTaxYear, requestSource, expectedStatus, expectedBody) =>
        s"Non‑TYS validation fails with ${expectedBody.code} error" in new NonTysTest {
          override val nino: String    = requestNino
          override val taxYear: String = requestTaxYear
          override val source: String  = requestSource

          val response: WSResponse = await(mtdRequest.get())
          response.status shouldBe expectedStatus
          response.json shouldBe expectedBody.asJson
        }
      }

      input.foreach { case (requestNino, requestTaxYear, requestSource, expectedStatus, expectedBody) =>
        s"TYS HIP validation fails with ${expectedBody.code} error" in new TysHipTest {
          override val nino: String    = requestNino
          override val taxYear: String = requestTaxYear
          override val source: String  = requestSource

          val response: WSResponse = await(mtdRequest.get())
          response.status shouldBe expectedStatus
          response.json shouldBe expectedBody.asJson
        }
      }
    }

    "downstream service error" when {

      def nonTysServiceErrorTest(downstreamStatus: Int, downstreamErrorCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns $downstreamErrorCode with status $downstreamStatus" in new NonTysTest {

          override def setupStubs(): Unit =
            DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamErrorCode))

          val response: WSResponse = await(mtdRequest.get())
          response.json shouldBe expectedBody.asJson
          response.status shouldBe expectedStatus
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      def tysHipServiceErrorTest(downstreamStatus: Int, downstreamErrorCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"TYS downstream returns $downstreamErrorCode with status $downstreamStatus" in new TysHipTest {

          override def setupStubs(): Unit =
            DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamErrorCode))

          val response: WSResponse = await(mtdRequest.get())
          response.json shouldBe expectedBody.asJson
          response.status shouldBe expectedStatus
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val nonTysErrors = List(
        (BAD_REQUEST, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_REQUEST", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_PERIOD_START", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_PERIOD_END", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE", BAD_REQUEST, RuleTaxYearRangeInvalidError),
        (BAD_REQUEST, "INVALID_SOURCE", BAD_REQUEST, RuleSourceInvalidError)
      )
      nonTysErrors.foreach(args => nonTysServiceErrorTest.tupled(args))

      val tysHipErrors = List(
        (BAD_REQUEST, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
        (UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE", BAD_REQUEST, RuleTaxYearRangeInvalidError),
        (BAD_REQUEST, "INVALID_SOURCE", BAD_REQUEST, RuleSourceInvalidError),
        (BAD_REQUEST, "INVALID_TAX_YEAR", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_START_DATE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_END_DATE", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_ALIGNED", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
        (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError)
      )
      tysHipErrors.foreach(args => tysHipServiceErrorTest.tupled(args))
    }
  }

  private trait Test {
    val taxYear: String
    val fromDate: String
    val toDate: String

    val nino   = "AA123456A"
    val source = "customer"

    val downstreamQueryParams: Map[String, String]

    def setupStubs(): Unit = ()

    def mtdRequest: WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/$nino/current-position/$taxYear/$source")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |  "origin": "HoD",
         |  "response": {
         |    "failures": [
         |      {
         |        "type": "$code",
         |        "reason": "message"
         |      }
         |    ]
         |  }
         |}
            """.stripMargin

  }

  private trait NonTysTest extends Test {
    val taxYear: String                            = "2019-20"
    val fromDate: String                           = "2019-04-06"
    val toDate: String                             = "2020-04-05"
    val downstreamQueryParams: Map[String, String] = Map("periodStart" -> fromDate, "periodEnd" -> toDate, "source" -> source)
    val downstreamUri: String                      = s"/income-tax/cis/deductions/$nino"
  }

  private trait TysHipTest extends Test {
    val taxYear: String                            = "2025-26"
    val fromDate: String                           = "2025-04-06"
    val toDate: String                             = "2026-04-05"
    private val downstreamTaxYear: String          = "25-26"
    val downstreamQueryParams: Map[String, String] = Map("startDate" -> fromDate, "endDate" -> toDate, "source" -> source)
    val downstreamUri: String                      = s"/itsa/income-tax/v1/$downstreamTaxYear/cis/deductions/$nino"
  }

}
