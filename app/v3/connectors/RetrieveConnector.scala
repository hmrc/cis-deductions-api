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

package v3.connectors

import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import shared.connectors.DownstreamUri.{HipUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v3.models.request.retrieve.RetrieveRequestData
import v3.models.response.retrieve.{CisDeductions, RetrieveResponseModel}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrieve(request: RetrieveRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveResponseModel[CisDeductions]]] = {

    import request._

    val path = s"income-tax/cis/deductions/$nino"

    lazy val downstreamUri1792 =
      if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1792")) {
        HipUri[RetrieveResponseModel[CisDeductions]](s"itsa/income-tax/v1/${taxYear.asTysDownstream}/cis/deductions/$nino")
      } else {
        IfsUri[RetrieveResponseModel[CisDeductions]](s"income-tax/cis/deductions/${taxYear.asTysDownstream}/$nino")
      }

    lazy val downstreamUri1572 = IfsUri[RetrieveResponseModel[CisDeductions]](path)

    val (downstreamUri, queryParams) = {
      if (taxYear.useTaxYearSpecificApi) {
        (
          downstreamUri1792,
          List("startDate" -> startDate, "endDate" -> endDate, "source" -> source.toString)
        )
      } else {
        (
          downstreamUri1572,
          List("periodStart" -> startDate, "periodEnd" -> endDate, "source" -> source.toString)
        )
      }
    }

    get(downstreamUri, queryParams)
  }

}
