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

import play.api.http.Status.{CREATED, OK}
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{DesUri, TaxYearSpecificIfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.create.CreateRequestData
import v2.models.response.create.CreateResponseModel

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateConnector @Inject() (val http: HttpClient, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def create(request: CreateRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[CreateResponseModel]] = {

    import request._

    val (downstreamUri, statusCode) = if (taxYear.useTaxYearSpecificApi) {
      (TaxYearSpecificIfsUri[CreateResponseModel](s"income-tax/${taxYear.asTysDownstream}/cis/deductions/$nino"), CREATED)
    } else {
      (DesUri[CreateResponseModel](s"income-tax/cis/deductions/$nino"), OK)
    }

    implicit val successCode: SuccessCode = SuccessCode(statusCode)

    post(body, downstreamUri)
  }

}
