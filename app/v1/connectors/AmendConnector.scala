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

import config.CisDeductionsApiFeatureSwitches
import shared.config.AppConfig
import shared.connectors.DownstreamUri.{DesUri, IfsUri, TaxYearSpecificIfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.amend.AmendRequestData

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit featureSwitches: CisDeductionsApiFeatureSwitches)
    extends BaseDownstreamConnector {

  def amendDeduction(
      request: AmendRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request._

    val path = s"income-tax/cis/deductions/$nino/submissionId/$submissionId"

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[Unit](s"income-tax/${taxYear.asTysDownstream}/cis/deductions/$nino/$submissionId")
    } else if (featureSwitches.isDesIf_MigrationEnabled) {
      IfsUri[Unit](path)
    } else {
      DesUri[Unit](path)
    }

    put(body, downstreamUri)
  }

}
