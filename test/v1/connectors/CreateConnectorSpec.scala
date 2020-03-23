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
import v1.models.request.{CreateRequestData, CreateRequestModel, PeriodDetails}
import v1.models.responseData.CreateResponseModel
import scala.concurrent.Future

class CreateConnectorSpec extends ConnectorSpec {

  val nino = Nino("AA123456A")
  val id = "123456789"

  class Test extends MockHttpClient with MockAppConfig {
    val connector: CreateConnector = new CreateConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "createDeductions" must {
    val request = CreateRequestData(nino, CreateRequestModel("","","","",Seq(PeriodDetails(0.00,"","",Some(0.00),0.00))))

    "post a CreateCisDeductionRequest body and return the result" in new Test {
      val outcome = Right(ResponseWrapper(id, CreateResponseModel(id)))

      MockedHttpClient
        .post(
          url = s"$baseUrl/cross-regime/deductions-placeholder/CIS/${request.nino}",
          body = request.body,
          requiredHeaders ="Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
        )
        .returns(Future.successful(outcome))

      await(connector.create(request)) shouldBe outcome
    }
  }
}