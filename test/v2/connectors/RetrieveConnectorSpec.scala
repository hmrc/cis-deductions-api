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

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v2.models.request.retrieve.RetrieveRequestData
import v2.models.response.retrieve.{CisDeductions, PeriodData, RetrieveResponseModel}

import scala.concurrent.Future

class RetrieveConnectorSpec extends ConnectorSpec {

  private val nino = "AA123456A"

  "Retrieve connector" when {
    "given a valid non-TYS request" must {
      "return a valid response from downstream" in new DesTest with Test {
        def fromDate = "2019-04-06"
        def toDate   = "2020-04-05"

        val outcome = Right(
          ResponseWrapper(
            correlationId,
            RetrieveResponseModel(
              Some(0.00),
              Some(0.00),
              Some(0.00),
              Seq(CisDeductions(
                request.fromDate,
                request.toDate,
                Some(""),
                "",
                Some(0.00),
                Some(0.00),
                Some(0.00),
                Seq(PeriodData("", "", Some(0.00), Some(0.00), Some(0.00), "", Some(""), request.source))
              ))
            )
          ))

        willGet(
          url = s"$baseUrl/income-tax/cis/deductions/${nino}",
          queryParams = List("periodStart" -> request.fromDate, "periodEnd" -> request.toDate, "source" -> request.source)
        ) returns Future.successful(outcome)

        val result: DownstreamOutcome[RetrieveResponseModel[CisDeductions]] = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

    "given a valid request for a TaxYearSpecific tax year" must {
      "return a 200 for success scenario" in new TysIfsTest with Test {
        def fromDate         = "2023-04-06"
        def toDate           = "2024-04-06"
        def taxYear: TaxYear = TaxYear.fromIso(toDate)

        val outcome = Right(
          ResponseWrapper(
            correlationId,
            RetrieveResponseModel(
              Some(0.00),
              Some(0.00),
              Some(0.00),
              Seq(CisDeductions(
                request.fromDate,
                request.toDate,
                Some(""),
                "",
                Some(0.00),
                Some(0.00),
                Some(0.00),
                Seq(PeriodData("", "", Some(0.00), Some(0.00), Some(0.00), "", Some(""), request.source))
              ))
            )
          ))

        willGet(
          url = s"$baseUrl/income-tax/cis/deductions/${taxYear.asTysDownstream}/$nino",
          queryParams = List("startDate" -> request.fromDate, "endDate" -> request.toDate, "source" -> request.source)
        ) returns Future.successful(outcome)

        val result: DownstreamOutcome[RetrieveResponseModel[CisDeductions]] = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    def fromDate: String
    def toDate: String

    protected val connector: RetrieveConnector = new RetrieveConnector(http = mockHttpClient, appConfig = mockAppConfig)
    protected val request: RetrieveRequestData = RetrieveRequestData(Nino(nino), fromDate, toDate, "contractor")

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

}