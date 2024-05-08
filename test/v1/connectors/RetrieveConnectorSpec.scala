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

package v1.connectors

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.outcomes.ResponseWrapper
import mocks.MockFeatureSwitches
import shared.models.domain.{DateRange, Nino, Source, TaxYear}
import v1.models.request.retrieve.RetrieveRequestData
import v1.models.response.retrieve.{CisDeductions, PeriodData, RetrieveResponseModel}

import java.time.LocalDate
import scala.concurrent.Future

class RetrieveConnectorSpec extends ConnectorSpec with MockFeatureSwitches{

  private val nino = "AA123456A"

  "Retrieve connector" when {
    "given a valid non-TYS request" must {
      "return a valid response from downstream when 'isDesIf_MigrationEnabled' is turned off" in new DesTest with Test {
        protected def fromDateStr = "2019-04-06"

        protected def toDateStr = "2020-04-05"

        MockFeatureSwitches.isDesIf_MigrationEnabled.returns(false)

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
                Seq(PeriodData("", "", Some(0.00), Some(0.00), Some(0.00), "", Some(""), request.source.toString))
              ))
            )
          ))

        willGet(
          url = s"$baseUrl/income-tax/cis/deductions/$nino",
          queryParams = List("periodStart" -> request.fromDate, "periodEnd" -> request.toDate, "source" -> request.source.toString)
        ) returns Future.successful(outcome)

        val result: DownstreamOutcome[RetrieveResponseModel[CisDeductions]] = await(connector.retrieve(request))
        result shouldBe outcome
      }


      "return a valid response from downstream when 'isDesIf_MigrationEnabled' is turned on" in new IfsTest with Test {
        protected def fromDateStr = "2019-04-06"

        protected def toDateStr = "2020-04-05"

        MockFeatureSwitches.isDesIf_MigrationEnabled.returns(true)

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
                Seq(PeriodData("", "", Some(0.00), Some(0.00), Some(0.00), "", Some(""), request.source.toString))
              ))
            )
          ))

        willGet(
          url = s"$baseUrl/income-tax/cis/deductions/$nino",
          queryParams = List("periodStart" -> request.fromDate, "periodEnd" -> request.toDate, "source" -> request.source.toString)
        ) returns Future.successful(outcome)

        val result: DownstreamOutcome[RetrieveResponseModel[CisDeductions]] = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

    "given a valid request for a TaxYearSpecific tax year" must {
      "return a 200 for success scenario" in new TysIfsTest with Test {
        protected def fromDateStr = "2023-04-06"
        protected def toDateStr   = "2024-04-06"

        private def taxYear: TaxYear = TaxYear.fromIso(toDateStr)

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
                Seq(PeriodData("", "", Some(0.00), Some(0.00), Some(0.00), "", Some(""), request.source.toString))
              ))
            )
          ))

        willGet(
          url = s"$baseUrl/income-tax/cis/deductions/${taxYear.asTysDownstream}/$nino",
          queryParams = List("startDate" -> request.fromDate, "endDate" -> request.toDate, "source" -> request.source.toString)
        ) returns Future.successful(outcome)

        val result: DownstreamOutcome[RetrieveResponseModel[CisDeductions]] = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    protected def fromDateStr: String
    protected def toDateStr: String

    protected val fromDate: LocalDate = LocalDate.parse(fromDateStr)
    protected val toDate: LocalDate   = LocalDate.parse(toDateStr)

    protected val dateRange: DateRange = DateRange(fromDate, toDate)

    protected val connector: RetrieveConnector = new RetrieveConnector(http = mockHttpClient, appConfig = mockAppConfig)
    protected val request: RetrieveRequestData = RetrieveRequestData(Nino(nino), dateRange, Source.`contractor`)

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

}
