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
import v1.fixtures.AmendRequestFixtures._
import v1.mocks.connectors.MockAmendConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.AmendRequestData
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendServiceSpec extends UnitSpec{

  val validNino = Nino("AA123456A")
  val id = "S4636A77V5KB8625U"

  val requestData = AmendRequestData(validNino,id,amendRequestObj)

  trait Test extends MockAmendConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    val service = new AmendService(
      connector = mockAmendConnector
    )
  }

    "service" when {
      "a service call is successful" should {
        "return a mapped result" in new Test {
          MockAmendConnector.amendDeduction(requestData)
            .returns(Future.successful(Right(ResponseWrapper("resultId",()))))

          await(service.amendDeductions(requestData)) shouldBe Right(ResponseWrapper("resultId", ()))
        }
      }
      "a service call is unsuccessful" should {
        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"return a $desErrorCode error is returned from the service" in new Test {

            MockAmendConnector.amendDeduction(requestData)
              .returns(Future.successful(Left(ResponseWrapper("resultId", DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.amendDeductions(requestData)) shouldBe Left(ErrorWrapper(Some("resultId"), Seq(error)))
          }

        val input = Seq(
          ("INVALID_IDVALUE", NinoFormatError),
          ("INVALID_DEDUCTION_DATE_FROM",DeductionFromDateFormatError),
          ("INVALID_DEDUCTION_DATE_TO", DeductionToDateFormatError),
          ("INVALID_DATE_FROM", FromDateFormatError),
          ("INVALID_DATE_TO", ToDateFormatError),
          ("INVALID_DEDUCTIONS_DATE_RANGE",RuleDeductionsDateRangeInvalidError),
          ("INVALID_DEDUCTIONS_TO_DATE_BEFORE_DEDUCTIONS_FROM_DATE",RuleToDateBeforeFromDateError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError),
          ("INVALID_NO_CHANGE", RuleNoChangeError)
        )
        input.foreach(args => (serviceError _).tupled(args))
      }
    }
}
