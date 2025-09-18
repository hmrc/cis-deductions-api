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

package v2.services

import models.errors.SubmissionIdFormatError
import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{
  DownstreamErrorCode,
  DownstreamErrors,
  ErrorWrapper,
  InternalError,
  MtdError,
  NinoFormatError,
  NotFoundError,
  RuleTaxYearNotSupportedError,
  TaxYearFormatError
}
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.connectors.MockDeleteConnector
import v2.models.domain.SubmissionId
import v2.models.request.delete.DeleteRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteServiceSpec extends UnitSpec with MockDeleteConnector {

  private val nino         = Nino("AA123456A")
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val taxYear      = TaxYear.fromMtd("2023-24")

  private implicit val correlationId: String = "X-123"

  private val requestData = DeleteRequestData(nino, SubmissionId(submissionId), Some(taxYear))

  trait Test {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new DeleteService(
      connector = mockDeleteConnector
    )

  }

  "service" should {
    "return a mapped result" when {
      "a service call is successful" in new Test {
        MockDeleteConnector
          .deleteDeduction(requestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

        await(service.deleteDeductions(requestData)) shouldBe Right(ResponseWrapper("resultId", ()))
      }
    }

    def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
      s"return ${error.code} error" when {
        s" $downstreamErrorCode error code is returned from the connector " in new Test {
          MockDeleteConnector
            .deleteDeduction(requestData)
            .returns(Future.successful(Left(ResponseWrapper("resultId", DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.deleteDeductions(requestData)) shouldBe Left(ErrorWrapper("resultId", error))
        }
      }

    val errors = List(
      "NO_DATA_FOUND"             -> NotFoundError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError,
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_SUBMISSION_ID"     -> SubmissionIdFormatError,
      "INVALID_CORRELATIONID"     -> InternalError
    )

    val extraTysErrors = List(
      "INVALID_TAX_YEAR"       -> TaxYearFormatError,
      "INVALID_SUBMISSIONID"   -> SubmissionIdFormatError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
  }

}
