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

package v1.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers._
import v1.mocks.services.{MockEnrolmentsAuthService, _}
import v1.models.audit._
import v1.models.domain._
import v1.models.errors._
import v1.models.hateoas.Method.{GET, POST}
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request._
import v1.models._
import v1.fixtures._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateRequestControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateRequestParser
    with MockCreateService
    with MockHateoasFactory
    with MockAuditService {

trait test {
  val hc = HeaderCarrier()

  val controller = new CreateRequestController(
    authService = mockEnrolmentsAuthService,
    lookupService = mockMtdIdLookupService,
    requestParser  = mockRequestDataParser,
    service = mockService,
    hateoasFactory = mockHateoasFactory,
    auditService = mockAuditService,
    cc = cc
  )

  MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
  MockedEnrolmentsAuthService.authoriseUser()

}

  private val nino = "AA123456A"

  val testHateoasLinks: Seq[Link] = Seq(
    Link(
      href = s"/deductions/cis/$nino/amendments",
      method = POST,
      rel = "self"
    )
  )

  def event(auditResponse: CreateAuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail]



}
