/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.services

import models.domain.CisSource
import models.errors.RuleSourceInvalidError
import shared.config.MockSharedAppConfig
import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v3.fixtures.RetrieveModels._
import v3.mocks.connectors.MockRetrieveConnector
import v3.models.request.retrieve.RetrieveRequestData
import v3.models.response.retrieve.{CisDeductions, RetrieveResponseModel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveServiceSpec extends UnitSpec with MockSharedAppConfig with MockRetrieveConnector {

  private val nino = Nino("AA123456A")

  private val taxYearRaw = "2019-20"
  private val taxYear    = TaxYear.fromMtd(taxYearRaw)

  private val tysTaxYearRaw = "2023-24"
  private val tysTaxYear    = TaxYear.fromMtd(tysTaxYearRaw)

  private val source                                 = CisSource.`contractor`
  val request: RetrieveRequestData                   = RetrieveRequestData(nino, taxYear, source)
  val tysRequest: RetrieveRequestData                = RetrieveRequestData(nino, tysTaxYear, source)
  val response: RetrieveResponseModel[CisDeductions] = retrieveCisDeductionsModel

  implicit val correlationId: String = "X-123"

  trait Test {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("controller", "retrievecis")

    val service = new RetrieveService(mockRetrieveConnector, mockSharedAppConfig)
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

      val errors = List(
        ("INVALID_CORRELATIONID", InternalError),
        ("INVALID_DATE_RANGE", RuleTaxYearRangeInvalidError),
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
        ("NO_DATA_FOUND", NotFoundError),
        ("INVALID_TAX_YEAR", InternalError),
        ("INVALID_PERIOD_START", InternalError),
        ("INVALID_PERIOD_END", InternalError),
        ("INVALID_SOURCE", RuleSourceInvalidError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )
      errors.foreach(args => (serviceError _).tupled(args))

      val extraTysErrors = List(
        ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError),
        ("INVALID_START_DATE", InternalError),
        ("INVALID_END_DATE", InternalError),
        ("TAX_YEAR_NOT_ALIGNED", InternalError)
      )
      extraTysErrors.foreach(args => (tysServiceError _).tupled(args))
    }
  }

}
