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

import api.connectors.DownstreamUri.{DesUri, IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.{AppConfig, FeatureSwitches}
import play.api.http.Status.{CREATED, OK}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.create.CreateRequestData
import v1.models.response.create.CreateResponseModel
import v2.models.response.retrieve.{CisDeductions, RetrieveResponseModel}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit featureSwitches: FeatureSwitches)
    extends BaseDownstreamConnector {

  def create(request: CreateRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateResponseModel]] = {

    import request._

    val path = s"income-tax/cis/deductions/$nino"

    val (downstreamUri, statusCode) = if (taxYear.useTaxYearSpecificApi) {
      (TaxYearSpecificIfsUri[CreateResponseModel](s"income-tax/${taxYear.asTysDownstream}/cis/deductions/$nino"), CREATED)
    } else if (featureSwitches.isDesIf_MigrationEnabled) {
      IfsUri[RetrieveResponseModel[CisDeductions]](path)
    } else {
      (DesUri[CreateResponseModel](s"income-tax/cis/deductions/$nino"), OK)
    }

    implicit val successCode: SuccessCode = SuccessCode(statusCode)

    post(body, downstreamUri)
  }

}
