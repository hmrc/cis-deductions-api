/*
 * Copyright 2022 HM Revenue & Customs
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

import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockCreateConnector
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amend.PeriodDetails
import v1.models.request.create.{CreateBody, CreateRequestData}
import v1.models.response.create.CreateResponseModel

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateServiceSpec extends UnitSpec {

  private val nino           = "AA123456A"
  implicit val correlationId = "X-123"
  private val submissionId   = "123456789"

  private val requestBody = CreateBody("", "", "", "", Seq(PeriodDetails(0.00, "", "", Some(0.00), Some(0.00))))

  private val requestData = CreateRequestData(Nino(nino), requestBody)

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

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockCreateCisDeductionsConnector
              .createCisDeduction(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.createDeductions(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_PAYLOAD", RuleIncorrectOrEmptyBodyError),
          ("INVALID_EMPREF", EmployerRefFormatError),
          ("INVALID_REQUEST_TAX_YEAR_ALIGN", RuleUnalignedDeductionsPeriodError),
          ("INVALID_REQUEST_DATE_RANGE", RuleDeductionsDateRangeInvalidError),
          ("INVALID_REQUEST_BEFORE_TAX_YEAR", RuleTaxYearNotEndedError),
          ("CONFLICT", RuleDuplicateSubmissionError),
          ("INVALID_REQUEST_DUPLICATE_MONTH", RuleDuplicatePeriodError),
          ("SERVER_ERROR", StandardDownstreamError),
          ("SERVICE_UNAVAILABLE", StandardDownstreamError),
          ("INVALID_CORRELATIONID", StandardDownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
