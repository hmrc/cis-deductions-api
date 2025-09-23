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

package v3.connectors

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v3.fixtures.AmendRequestFixtures.*
import v3.models.domain.SubmissionId
import v3.models.request.amend.AmendRequestData

import scala.concurrent.Future

class AmendConnectorSpec extends ConnectorSpec {

  "AmendConnector" should {

    "return a result" when {

      "the downstream call is successful from Ifs" in new IfsTest with Test {

        def taxYearIso: String = "2019-07-05"

        private val expectedOutcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = url"$baseUrl/income-tax/cis/deductions/$nino/submissionId/$submissionId",
          body = amendRequestObj
        )
          .returns(Future.successful(expectedOutcome))

        val result: DownstreamOutcome[Unit] = await(connector.amendDeduction(request))

        result shouldBe expectedOutcome
      }

      "the downstream call is successful for a TYS tax year" in new IfsTest with Test {

        def taxYearIso: String = "2023-12-01"

        private val expectedOutcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = url"$baseUrl/income-tax/23-24/cis/deductions/$nino/$submissionId",
          body = amendRequestObj
        )
          .returns(Future.successful(expectedOutcome))

        val result: DownstreamOutcome[Unit] = await(connector.amendDeduction(request))

        result shouldBe expectedOutcome

      }
    }
  }

  trait Test {
    self: ConnectorTest =>

    val nino         = "AA123456A"
    val submissionId = "S4636A77V5KB8625U"

    def taxYearIso: String

    val connector: AmendConnector = new AmendConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

    lazy val request: AmendRequestData = AmendRequestData(Nino(nino), SubmissionId(submissionId), TaxYear.fromIso(taxYearIso), amendRequestObj)
  }

}
