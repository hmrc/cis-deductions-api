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
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.DeleteRequestParser
import v1.models.request.delete.DeleteRawData
import v1.services.DeleteService
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DeleteController @Inject() (val authService: EnrolmentsAuthService,
                                  val lookupService: MtdIdLookupService,
                                  requestParser: DeleteRequestParser,
                                  service: DeleteService,
                                  auditService: AuditService,
                                  cc: ControllerComponents,
                                  val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "DeleteController",
      endpointName = "deleteEndpoint"
    )

  def delete(nino: String, submissionId: String, taxYear: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = DeleteRawData(nino, submissionId, taxYear)

      val requestHandler = RequestHandler
        .withParser(requestParser)
        .withService(service.deleteDeductions)
        .withAuditing {
          val params = Map("nino" -> nino, "submissionId" -> submissionId) ++ taxYear.map(x => "taxYear" -> x)

          AuditHandler(
            auditService = auditService,
            auditType = "DeleteCisDeductionsForSubcontractor",
            transactionName = "delete-cis-deductions-for-subcontractor",
            params = params,
            requestBody = None
          )
        }

      requestHandler.handleRequest(rawData)
    }

}
