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
import v1.fixtures.RetrieveModels._
import v1.mocks.connectors.MockRetrieveConnector
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieve.RetrieveRequestData
import v1.models.response.retrieve.{CisDeductions, RetrieveResponseModel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveServiceSpec extends UnitSpec {

  private val nino     = Nino("AA123456A")
  private val fromDate = "2019-04-06"
  private val toDate   = "2020-04-05"
  private val source   = "Contractor"

  val request: RetrieveRequestData                   = RetrieveRequestData(nino, fromDate, toDate, source)
  val response: RetrieveResponseModel[CisDeductions] = retrieveCisDeductionsModel

  implicit val correlationId = "X-123"

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

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockRetrieveCisDeductionsConnector
            .retrieveCisDeduction(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

          await(service.retrieveDeductions(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }
      val input = Seq(
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
        ("NO_DATA_FOUND", NotFoundError),
        ("INVALID_DATE_RANGE", RuleDateRangeOutOfDate),
        ("INVALID_PERIOD_START", FromDateFormatError),
        ("INVALID_PERIOD_END", ToDateFormatError),
        ("INVALID_SOURCE", RuleSourceError),
        ("SERVER_ERROR", StandardDownstreamError),
        ("SERVICE_UNAVAILABLE", StandardDownstreamError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

}
