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
import v1.models.outcomes.ResponseWrapper
import v1.models.request.AmendRequestData
import v1.models.responseData.AmendResponse
import v1.fixtures.AmendRequestFixtures._
import v1.models.errors.{DesErrorCode, DesErrors}


import scala.concurrent.Future

class AmendConnectorSpec extends ConnectorSpec {

  val nino = Nino("AA123456A")
  val id = "S4636A77V5KB8625U"

  class Test extends MockHttpClient with MockAppConfig {
    val connector: AmendConnector = new AmendConnector(http = mockHttpClient, appConfig = mockAppConfig)
    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desCisUrl returns "cross-regime/deductions-placeholder/CIS"

  }

  "amend" should {
    val request = AmendRequestData(nino, id, amendRequestObj)

    "return a result" when {
      "the downstream call is successful" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, AmendResponse(id)))
        MockedHttpClient.
          put(
            url = s"$baseUrl/cross-regime/deductions-placeholder/CIS/${request.nino}/amendments/${request.id}",
            body = request.body,
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          ).returns(Future.successful(outcome))
        await(connector.amendDeduction(request)) shouldBe outcome
      }
    }
    "return a Des Error code" when {
      "the http client returns a Des Error code" in new Test {
        val outcome = Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode("error"))))

        MockedHttpClient
          .put(
            url = s"$baseUrl/cross-regime/deductions-placeholder/CIS/${request.nino}/amendments/${request.id}",
            body = request.body,
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          )
          .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode("error"))))))

        val result: DesOutcome[AmendResponse] = await(connector.amendDeduction(request))
        result shouldBe outcome
      }
    }
  }
}