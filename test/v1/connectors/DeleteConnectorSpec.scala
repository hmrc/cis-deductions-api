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
import v1.models.request.DeleteRequest
import v1.models.responseData.listDeductions.ListResponseModel

import scala.concurrent.Future

class DeleteConnectorSpec extends ConnectorSpec {

  val nino = Nino("AA123456A")
  val id = "S4636A77V5KB8625U"


  class Test extends MockHttpClient with MockAppConfig {
    val connector: DeleteConnector = new DeleteConnector(http = mockHttpClient, appConfig = mockAppConfig)
    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desCisUrl returns "cross-regime/deductions-placeholder/CIS"
  }

  "delete" should {
    val request: DeleteRequest = DeleteRequest(nino, id)

    "return a result" when {
      "the downstream call is successful" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient.
          delete(
            url = s"$baseUrl/cross-regime/deductions-placeholder/CIS/${request.nino}/amendments/${request.id}",
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          ).returns(Future.successful(outcome))

        await(connector.delete(request)) shouldBe outcome
      }
    }

  "return a DES error code" when {
    "the http client returns a Des Error code" in new Test {
      val outcome = Left(ResponseWrapper(correlationId,DesErrors.single(DesErrorCode("error"))))

      MockedHttpClient.delete[DesOutcome[DeleteRequest]](url = s"$baseUrl/cross-regime/deductions-placeholder/CIS/${request.nino}/amendments/${request.id}")
        .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode("error"))))))

      val result: DesOutcome[DeleteRequest] = await(connector.delete(request))
      result shouldBe outcome
    }
  }

  }
}
