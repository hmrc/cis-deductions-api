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

package auth

import play.api.http.Status.CREATED
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import shared.auth.AuthMainAgentsOnlyISpec
import v2.fixtures.CreateRequestFixtures.{createDeductionResponseBodyTys, requestBodyJsonTys}

class CisDeductionsApiAuthMainAgentsOnlyISpec extends AuthMainAgentsOnlyISpec {

  val callingApiVersion = "2.0"

  val supportingAgentsNotAllowedEndpoint = "create"

  val mtdUrl = s"/$nino/amendments"

  def sendMtdRequest(request: WSRequest): WSResponse = await(request.post(requestBodyJsonTys))

  val downstreamUri: String = s"/income-tax/23-24/cis/deductions/$nino"

  override val downstreamSuccessStatus: Int = CREATED

  val maybeDownstreamResponseJson: Option[JsValue] = Option(createDeductionResponseBodyTys)

}
