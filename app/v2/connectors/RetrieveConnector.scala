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

package v2.connectors

import config.CisDeductionsApiFeatureSwitches
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{DesUri, IfsUri, TaxYearSpecificIfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.retrieve.RetrieveRequestData
import v2.models.response.retrieve.{CisDeductions, RetrieveResponseModel}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveConnector @Inject() (val http: HttpClient, val appConfig: SharedAppConfig)(implicit featureSwitches: CisDeductionsApiFeatureSwitches)
    extends BaseDownstreamConnector {

  def retrieve(request: RetrieveRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveResponseModel[CisDeductions]]] = {

    import request._

    val path = s"income-tax/cis/deductions/$nino"

    val (downstreamUri, queryParams) = {
      if (taxYear.useTaxYearSpecificApi) {
        (
          TaxYearSpecificIfsUri[RetrieveResponseModel[CisDeductions]](s"income-tax/cis/deductions/${taxYear.asTysDownstream}/$nino"),
          List("startDate" -> startDate, "endDate" -> endDate, "source" -> source.toString)
        )
      } else if (featureSwitches.isDesIf_MigrationEnabled) {
        (
          IfsUri[RetrieveResponseModel[CisDeductions]](path),
          List("periodStart" -> startDate, "periodEnd" -> endDate, "source" -> source.toString)
        )
      } else {
        (
          DesUri[RetrieveResponseModel[CisDeductions]](path),
          List("periodStart" -> startDate, "periodEnd" -> endDate, "source" -> source.toString)
        )
      }
    }

    get(downstreamUri, queryParams)
  }

}
