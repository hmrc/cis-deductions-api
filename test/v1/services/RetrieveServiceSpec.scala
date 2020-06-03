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
import v1.fixtures.RetrieveModels._
import v1.mocks.connectors.MockRetrieveConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.RetrieveRequestData
import v1.models.responseData.{DeductionsDetails, RetrieveResponseModel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveServiceSpec extends UnitSpec {

  private val nino = Nino("AA123456A")
  private val correlationId = "X-123"
  private val fromDate = "2019-04-06"
  private val toDate = "2020-04-05"
  private val source = "Contractor"

  val request: RetrieveRequestData = RetrieveRequestData(nino, fromDate, toDate, source)
  val response: RetrieveResponseModel[DeductionsDetails] = retrieveCisDeductionsModel

  trait Test extends MockRetrieveConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("controller", "retrievecis")

    val service = new RetrieveService(mockRetrieveConnector)
  }

  "RetrieveDeductions" should {
    "return a valid response" when {
      "a valid request is supplied" in new Test {
        MockRetrieveCisDeductionsConnector.retrieveCisDeduction(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.retrieveDeductions(request)) shouldBe Right(ResponseWrapper(correlationId,response))
      }
    }

    "return error response" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockRetrieveCisDeductionsConnector.retrieveCisDeduction(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.retrieveDeductions(request)) shouldBe Left(ErrorWrapper(Some(correlationId), Seq(error)))
        }
      val input = Seq(
        ("INVALID_IDVALUE" , NinoFormatError),
        ("INVALID_DATE_FROM", FromDateFormatError),
        ("INVALID_DATE_TO", ToDateFormatError),
        ("NOT_FOUND", NotFoundError),
        ("SERVER_ERROR", DownstreamError),
        ("SERVICE_UNAVAILABLE", DownstreamError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}