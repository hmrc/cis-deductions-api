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

package v1.connectors

import mocks.MockAppConfig
import uk.gov.hmrc.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.errors.{DesErrorCode, DesErrors}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.RetrieveRequestData
import v1.models.responseData.{DeductionsDetails, RetrieveResponseModel, PeriodDeductions}

import scala.concurrent.Future

class RetrieveConnectorSpec extends ConnectorSpec {

  val nino = Nino("AA123456A")

  class Test extends MockHttpClient with MockAppConfig {
    val connector: RetrieveConnector = new RetrieveConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desCisUrl returns "income-tax/cis/deductions"
  }

  "retrieve" should {
    "return a Retrieve Deductions response when a source is supplied" in new Test {
      val request = RetrieveRequestData(nino, "2019-04-05", "2020-04-06", "contractor")

      val outcome = Right(ResponseWrapper(correlationId, RetrieveResponseModel(
        Seq(DeductionsDetails(Some(""),request.fromDate,request.toDate,"","",
          Seq(PeriodDeductions(0.00,"","",Some(0.00),0.00,"",request.source))))
      )))

      MockedHttpClient.get(
        url = s"$baseUrl/income-tax/cis/deductions/${nino.nino}/current-position" +
          s"?fromDate=${request.fromDate}&toDate=${request.toDate}&source=${request.source}",
        requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
      ).returns(Future.successful(outcome))

      await(connector.retrieve(request)) shouldBe outcome
    }

    "return a Des Error code" when {
      "the http client returns a Des Error code" in new Test {
        val request = RetrieveRequestData(nino, "2019-04-05", "2020-04-06", "contractor")

        val outcome = Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode("error"))))

        MockedHttpClient.get[DesOutcome[RetrieveResponseModel[DeductionsDetails]]](s"$baseUrl/income-tax/cis/deductions" +
          s"/${nino.nino}/current-position" +
          s"?fromDate=${request.fromDate}&toDate=${request.toDate}&source=${request.source}")
          .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode("error"))))))

        val result: DesOutcome[RetrieveResponseModel[DeductionsDetails]] = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }
  }
}