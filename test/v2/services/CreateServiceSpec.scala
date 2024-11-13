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

package v2.services

import models.errors._
import shared.controllers.EndpointLogContext
import shared.models.domain.Nino
import shared.models.errors.{
  DownstreamErrorCode,
  DownstreamErrors,
  ErrorWrapper,
  InternalError,
  MtdError,
  NinoFormatError,
  RuleTaxYearNotEndedError,
  RuleTaxYearNotSupportedError
}
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.connectors.MockCreateConnector
import v2.models.request.amend.PeriodDetails
import v2.models.request.create
import v2.models.request.create.CreateBody
import v2.models.response.create.CreateResponseModel

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateServiceSpec extends UnitSpec {

  private val nino                   = "AA123456A"
  implicit val correlationId: String = "X-123"
  private val submissionId           = "123456789"

  private val requestBody =
    CreateBody(fromDate = "2020-05-06", toDate = "2020-06-05", "", "", Seq(PeriodDetails(0.00, "", "", Some(0.00), Some(0.00))))

  private val requestData = create.CreateRequestData(Nino(nino), requestBody)

  trait Test extends MockCreateConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateService(
      connector = mockCreateConnector
    )

  }

  "service" when {
    "service call successsful" must {
      "return mapped result" in new Test {
        MockCreateCisDeductionsConnector
          .createCisDeduction(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, CreateResponseModel(submissionId)))))

        await(service.createDeductions(requestData)) shouldBe Right(ResponseWrapper(correlationId, CreateResponseModel(submissionId)))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockCreateCisDeductionsConnector
              .createCisDeduction(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.createDeductions(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_PAYLOAD", InternalError),
          ("INVALID_EMPREF", EmployerRefFormatError),
          ("INVALID_REQUEST_TAX_YEAR_ALIGN", RuleUnalignedDeductionsPeriodError),
          ("INVALID_REQUEST_DATE_RANGE", RuleDeductionsDateRangeInvalidError),
          ("INVALID_REQUEST_BEFORE_TAX_YEAR", RuleTaxYearNotEndedError),
          ("CONFLICT", RuleDuplicateSubmissionError),
          ("INVALID_REQUEST_DUPLICATE_MONTH", RuleDuplicatePeriodError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError),
          ("INVALID_CORRELATIONID", InternalError)
        )

        val extraTysErrors = List(
          ("INVALID_TAX_YEAR", InternalError),
          ("INVALID_CORRELATION_ID", InternalError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError),
          ("INVALID_TAX_YEAR_ALIGN", RuleUnalignedDeductionsPeriodError),
          ("EARLY_SUBMISSION", RuleTaxYearNotEndedError),
          ("INVALID_DATE_RANGE", RuleDeductionsDateRangeInvalidError),
          ("DUPLICATE_MONTH", RuleDuplicatePeriodError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
