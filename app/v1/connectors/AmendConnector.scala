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

import config.AppConfig
import javax.inject.Inject
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import v1.connectors.httpparsers.StandardDesHttpParser.SuccessCode
import play.api.http.Status.CREATED
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.request.{AmendRequest, AmendRequestData}
import v1.models.responseData.AmendResponse
import v1.connectors.httpparsers.StandardDesHttpParser._

import scala.concurrent.{ExecutionContext, Future}



class AmendConnector @Inject()(val http: HttpClient,
                               val appConfig: AppConfig) extends BaseDesConnector  {

  def amendDeduction(request: AmendRequestData)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DesOutcome[AmendResponse]] = {

    implicit val successCode: SuccessCode = SuccessCode(CREATED)

    put(
      body = request.body,
      DesUri[AmendResponse](s"deductions/cis/${request.nino}/amendments/${request.id}")
    )
  }
}
