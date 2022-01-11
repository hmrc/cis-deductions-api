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

package v1.connectors

import mocks.MockAppConfig
import v1.models.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.errors.{DesErrorCode, DesErrors}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieve.RetrieveRequestData
import v1.models.response.retrieve.{CisDeductions, PeriodData, RetrieveResponseModel}

import scala.concurrent.Future

class RetrieveConnectorSpec extends ConnectorSpec {

  val nino = "AA123456A"

  class Test extends MockHttpClient with MockAppConfig {
    val connector: RetrieveConnector = new RetrieveConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desCisUrl returns "income-tax/cis/deductions"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "retrieve" should {
    "return a Retrieve Deductions response when a source is supplied" in new Test {
      val request: RetrieveRequestData = RetrieveRequestData(Nino(nino), "2019-04-05", "2020-04-06", "contractor")

      val outcome = Right(ResponseWrapper(correlationId, RetrieveResponseModel( Some(0.00), Some(0.00), Some(0.00),
        Seq(CisDeductions(request.fromDate, request.toDate,Some(""),"",Some(0.00),Some(0.00),Some(0.00),
          Seq(PeriodData("","",Some(0.00),Some(0.00), Some(0.00), "", Some(""),request.source))))
      )))

      MockedHttpClient.get(
        url = s"$baseUrl/income-tax/cis/deductions/${nino}" +
          s"?periodStart=${request.fromDate}&periodEnd=${request.toDate}&source=${request.source}",
        dummyDesHeaderCarrierConfig,
        desRequestHeaders,
        Seq("AnotherHeader" -> "HeaderValue")
      ).returns(Future.successful(outcome))

      await(connector.retrieve(request)) shouldBe outcome
    }

    "return a Des Error code" when {
      "the http client returns a Des Error code" in new Test {
        val request: RetrieveRequestData = RetrieveRequestData(Nino(nino), "2019-04-05", "2020-04-06", "contractor")

        val outcome = Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode("error"))))

        MockedHttpClient.get[DesOutcome[RetrieveResponseModel[CisDeductions]]](s"$baseUrl/income-tax/cis/deductions/${nino}" +
          s"?periodStart=${request.fromDate}&periodEnd=${request.toDate}&source=${request.source}",
          dummyDesHeaderCarrierConfig,
          desRequestHeaders,
          Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode("error"))))))

        val result: DesOutcome[RetrieveResponseModel[CisDeductions]] = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }
  }
}
