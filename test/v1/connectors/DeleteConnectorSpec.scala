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
import v1.mocks.MockFeatureSwitches
import shared.models.domain.{Nino, SubmissionId, TaxYear}
import v1.models.request.delete.DeleteRequestData

import scala.concurrent.Future

class DeleteConnectorSpec extends ConnectorSpec with MockFeatureSwitches{

  private val nino         = "AA123456A"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  "DeleteConnector" when {
    "called for a non-TYS tax year" should {
      "return a successful result from Des" in new DesTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        MockFeatureSwitches.isDesIf_MigrationEnabled.returns(false)

        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(url = s"$baseUrl/income-tax/cis/deductions/$nino/submissionId/${request.submissionId}") returns Future.successful(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.delete(request))
        result shouldBe outcome
      }

      "return a successful result from Ifs" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        MockFeatureSwitches.isDesIf_MigrationEnabled.returns(true)

        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(url = s"$baseUrl/income-tax/cis/deductions/$nino/submissionId/${request.submissionId}") returns Future.successful(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.delete(request))
        result shouldBe outcome
      }

    }

    "called for a nTax Year Specific tax year" should {
      "return a successful result" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(url = s"$baseUrl/income-tax/cis/deductions/${taxYear.asTysDownstream}/$nino/submissionId/${request.submissionId}") returns Future
          .successful(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.delete(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: DeleteConnector = new DeleteConnector(http = mockHttpClient, appConfig = mockAppConfig)
    protected val request: DeleteRequestData = DeleteRequestData(Nino(nino), SubmissionId(submissionId), Some(taxYear))
  }

}
