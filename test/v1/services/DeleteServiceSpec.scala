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
import v1.mocks.connectors.MockDeleteConnector
import v1.models.domain.{Nino, TaxYear}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.delete.DeleteRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteServiceSpec extends UnitSpec {

  private val nino         = Nino("AA123456A")
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val taxYear      = TaxYear.fromMtd("2023-24")

  implicit val correlationId = "X-123"

  val requestData = DeleteRequestData(nino, submissionId, Some(taxYear))

  trait Test extends MockDeleteConnector {
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
        s" ${downstreamErrorCode} error code is returned from the connector " in new Test {
          MockDeleteConnector
            .deleteDeduction(requestData)
            .returns(Future.successful(Left(ResponseWrapper("resultId", DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.deleteDeductions(requestData)) shouldBe Left(ErrorWrapper("resultId", error))
        }
      }

    val errors = Seq(
      ("NO_DATA_FOUND", NotFoundError),
      ("SERVER_ERROR", StandardDownstreamError),
      ("SERVICE_UNAVAILABLE", StandardDownstreamError),
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_SUBMISSION_ID"     -> SubmissionIdFormatError,
      "INVALID_CORRELATIONID"     -> StandardDownstreamError
    )

    val extraTysErrors = Seq(
      ("INVALID_TAX_YEAR", TaxYearFormatError),
      ("INVALID_SUBMISSIONID", SubmissionIdFormatError),
      ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
    )

    (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
  }

}
