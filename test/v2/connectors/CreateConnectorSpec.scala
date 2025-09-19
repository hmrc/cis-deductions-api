/*
 * Copyright 2025 HM Revenue & Customs
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

import shared.connectors.ConnectorSpec
import shared.models.domain.Nino
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.fixtures.CreateRequestFixtures.parsedRequestData
import v2.models.request.create
import v2.models.response.create.CreateResponseModel

import scala.concurrent.Future

class CreateConnectorSpec extends ConnectorSpec {

  trait Test { self: ConnectorTest =>
    val toDate: String

    val connector: CreateConnector = new CreateConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

    val outcome = Right(ResponseWrapper(correlationId, CreateResponseModel("123456789")))

    lazy val request = create.CreateRequestData(Nino("AA123456A"), parsedRequestData.copy(toDate = toDate))
  }

  "create" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new DesTest with Test {
        val toDate = "2022-06-01"

        willPost(
          url = url"$baseUrl/income-tax/cis/deductions/AA123456A",
          body = request.body
        ).returns(Future.successful(outcome))

        await(connector.create(request)) shouldBe outcome
      }
    }

    "return the expected response for a TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        val toDate = "2023-06-01"

        willPost(
          url = url"$baseUrl/income-tax/23-24/cis/deductions/AA123456A",
          body = request.body
        ).returns(Future.successful(outcome))

        await(connector.create(request)) shouldBe outcome
      }
    }
  }

}
