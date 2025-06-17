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

package v2.connectors

import models.domain.CisSource
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.models.request.retrieve.RetrieveRequestData
import v2.models.response.retrieve.{CisDeductions, PeriodData, RetrieveResponseModel}

import scala.concurrent.Future

class RetrieveConnectorSpec extends ConnectorSpec {

  private val nino = "AA123456A"

  "Retrieve connector" when {
    "given a valid non-TYS request" must {
      "return a valid response from downstream Ifs" in new IfsTest with Test {
        protected def startDate = "2019-04-06"

        protected def endDate = "2020-04-05"

        val outcome: Right[Nothing, ResponseWrapper[RetrieveResponseModel[CisDeductions]]] = Right(
          ResponseWrapper(
            correlationId,
            RetrieveResponseModel(
              Some(0.00),
              Some(0.00),
              Some(0.00),
              List(CisDeductions(
                request.startDate,
                request.endDate,
                Some(""),
                "",
                Some(0.00),
                Some(0.00),
                Some(0.00),
                List(PeriodData("", "", Some(0.00), Some(0.00), Some(0.00), "", Some(""), request.source))
              ))
            )
          ))

        willGet(
          url = url"$baseUrl/income-tax/cis/deductions/$nino",
          parameters = List("periodStart" -> request.startDate, "periodEnd" -> request.endDate, "source" -> request.source.toString)
        ) returns Future.successful(outcome)

        val result: DownstreamOutcome[RetrieveResponseModel[CisDeductions]] = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

    "given a valid request for a TaxYearSpecific tax year" must {
      "return a 200 for success scenario" in new TysIfsTest with Test {
        protected def startDate = "2023-04-06"
        protected def endDate   = "2024-04-06"

        val outcome: Right[Nothing, ResponseWrapper[RetrieveResponseModel[CisDeductions]]] = Right(
          ResponseWrapper(
            correlationId,
            RetrieveResponseModel(
              Some(0.00),
              Some(0.00),
              Some(0.00),
              List(CisDeductions(
                request.startDate,
                request.endDate,
                Some(""),
                "",
                Some(0.00),
                Some(0.00),
                Some(0.00),
                List(PeriodData("", "", Some(0.00), Some(0.00), Some(0.00), "", Some(""), request.source))
              ))
            )
          ))

        willGet(
          url = url"$baseUrl/income-tax/cis/deductions/${taxYear.asTysDownstream}/$nino",
          parameters = List("startDate" -> request.startDate, "endDate" -> request.endDate, "source" -> request.source.toString)
        ) returns Future.successful(outcome)

        val result: DownstreamOutcome[RetrieveResponseModel[CisDeductions]] = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    protected def startDate: String
    protected def endDate: String

    protected val taxYear: TaxYear = TaxYear.fromIso(endDate)

    protected val connector: RetrieveConnector = new RetrieveConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)
    protected val request: RetrieveRequestData = RetrieveRequestData(Nino(nino), taxYear, CisSource.`contractor`)
  }

}
