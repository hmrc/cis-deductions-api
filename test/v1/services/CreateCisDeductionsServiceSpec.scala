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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.services

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockCreateCisDeductionsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.requestData._
import v1.models.responseData.CreateCisDeductionsResponseModel

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateCisDeductionsServiceSpec extends UnitSpec {

  private val nino = "AA123456A"
  private val correlationId = "X-123"
  private val id = "123456789"

  private val requestBody = CreateCisDeductionsRequestModel("","","","",Seq(PeriodData(0.00,"","",Some(0.00),0.00)))

  private val requestData = CreateCisDeductionsRequestData(Nino(nino), requestBody)

  trait Test extends MockCreateCisDeductionsConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateCisDeductionsService(
      connector = mockCreateCisDeductionsConnector
    )
  }

  "service" when {
    "service call successsful" must {
      "return mapped result" in new Test {
        MockCreateCisDeductionsConnector.createCisDeduction(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId,CreateCisDeductionsResponseModel(id)))))

        await(service.createDeductions(requestData)) shouldBe Right(ResponseWrapper(correlationId, CreateCisDeductionsResponseModel(id)))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockCreateCisDeductionsConnector.createCisDeduction(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.createDeductions(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
          }

        val input = Seq(
          ("INVALID_IDVALUE" , NinoFormatError),
          ("INVALID_DEDUCTION_DATE_FROM" , DeductionFromDateFormatError),
          ("INVALID_DEDUCTION_DATE_TO" , DeductionToDateFormatError),
          ("INVALID_DATE_FROM" , FromDateFormatError),
          ("INVALID_DATE_TO" , ToDateFormatError),
          ("INVALID_DEDUCTIONS_DATE_RANGE" , RuleDateRangeInvalidError),
          ("INVALID_DEDUCTIONS_TO_DATE_BEFORE_DEDUCTIONS_FROM_DATE" , RuleToDateBeforeFromDateError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
