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

package v1.controllers

import api.controllers.{AuditHandler, AuthorisedController, EndpointLogContext, RequestContext, RequestHandler}
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.AmendRequestParser
import v1.models.request.amend.AmendRawData
import v1.services.AmendService
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AmendController @Inject() (val authService: EnrolmentsAuthService,
                                 val lookupService: MtdIdLookupService,
                                 requestParser: AmendRequestParser,
                                 service: AmendService,
                                 auditService: AuditService,
                                 cc: ControllerComponents,
                                 val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "AmendController",
      endpointName = "amendEndpoint"
    )

  def amend(nino: String, submissionId: String): Action[JsValue] = authorisedAction(nino).async(parse.json) { implicit request =>
    implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

    val rawData = AmendRawData(nino, submissionId, request.body)

    val requestHandler = RequestHandler
      .withParser(requestParser)
      .withService(service.amendDeductions)
      .withAuditing(AuditHandler(
        auditService = auditService,
        auditType = "AmendCisDeductionsForSubcontractor",
        transactionName = "amend-cis-deductions-for-subcontractor",
        params = Map("nino" -> nino, "submissionId" -> submissionId),
        requestBody = Some(request.body)
      ))

    requestHandler.handleRequest(rawData)
  }

}
