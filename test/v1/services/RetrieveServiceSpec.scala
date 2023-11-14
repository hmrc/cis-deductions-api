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

package v1.services

import api.models.outcomes.ResponseWrapper
import shared.UnitSpec
import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, Source}
import shared.models.errors._
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.RetrieveModels._
import v1.mocks.connectors.MockRetrieveConnector
import v1.models.request.retrieve.RetrieveRequestData
import v1.models.response.retrieve.{CisDeductions, RetrieveResponseModel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveServiceSpec extends UnitSpec {

  private val nino        = Nino("AA123456A")
  private val fromDate    = "2019-04-06"
  private val toDate      = "2020-04-05"
  private val tysFromDate = "2023-04-06"
  private val tysToDate   = "2024-04-05"
  private val source      = Source.`contractor`

  private val request    = RetrieveRequestData(nino, fromDate, toDate, source)
  private val tysRequest = RetrieveRequestData(nino, tysFromDate, tysToDate, source)

  private val response: RetrieveResponseModel[CisDeductions] = retrieveCisDeductionsModel

  private implicit val correlationId: String = "X-123"

  trait Test extends MockRetrieveConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("controller", "retrievecis")

    val service = new RetrieveService(mockRetrieveConnector)
  }

  "RetrieveDeductions" should {
    "return a valid response" when {
      "a valid request is supplied" in new Test {
        MockRetrieveCisDeductionsConnector
          .retrieveCisDeduction(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.retrieveDeductions(request)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "return error response" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        runTest(downstreamErrorCode, error, request)

      def tysServiceError(downstreamErrorCode: String, error: MtdError): Unit =
        runTest(downstreamErrorCode, error, tysRequest)

      def runTest(downstreamErrorCode: String, error: MtdError, request: RetrieveRequestData): Unit =
        s"downstream returns $downstreamErrorCode, for TY ${request.taxYear.year}" in new Test {

          MockRetrieveCisDeductionsConnector
            .retrieveCisDeduction(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.retrieveDeductions(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = Seq(
        ("INVALID_DATE_RANGE", RuleDateRangeOutOfDateError),
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
        ("NO_DATA_FOUND", NotFoundError),
        ("INVALID_TAX_YEAR", InternalError),
        ("INVALID_PERIOD_START", FromDateFormatError),
        ("INVALID_PERIOD_END", ToDateFormatError),
        ("INVALID_SOURCE", RuleSourceInvalidError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )
      errors.foreach(args => (serviceError _).tupled(args))

      val extraTysErrors = Seq(
        ("INVALID_DATE_RANGE", RuleTaxYearRangeInvalidError),
        ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
      )
      extraTysErrors.foreach(args => (tysServiceError _).tupled(args))
    }
  }

}
