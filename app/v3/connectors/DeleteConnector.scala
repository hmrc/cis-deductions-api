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

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v3.models.request.delete.DeleteRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteConnector @Inject() (val http: HttpClient, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def delete(request: DeleteRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request._

    val downstreamUri =
      taxYear match {
        case Some(taxYear) if taxYear.useTaxYearSpecificApi =>
          TaxYearSpecificIfsUri[Unit](s"income-tax/cis/deductions/${taxYear.asTysDownstream}/$nino/submissionId/$submissionId")
        case _ =>
          IfsUri[Unit](s"income-tax/cis/deductions/$nino/submissionId/$submissionId")
      }

    delete(downstreamUri)
  }

}
