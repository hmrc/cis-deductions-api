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

package v3.controllers

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers._
import shared.routing.Version
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator
import v3.controllers.validators.DeleteValidatorFactory
import v3.services.DeleteService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DeleteController @Inject() (val authService: EnrolmentsAuthService,
                                  val lookupService: MtdIdLookupService,
                                  validatorFactory: DeleteValidatorFactory,
                                  service: DeleteService,
                                  auditService: AuditService,
                                  cc: ControllerComponents,
                                  val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {
  override val endpointName: String = "delete"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "DeleteController",
      endpointName = "deleteEndpoint"
    )

  def delete(nino: String, submissionId: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, submissionId, taxYear)
      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.deleteDeductions)
        .withAuditing {
          val params = Map("nino" -> nino, "submissionId" -> submissionId, "taxYear" -> taxYear)

          AuditHandler(
            auditService = auditService,
            auditType = "DeleteCisDeductionsForSubcontractor",
            transactionName = "delete-cis-deductions-for-subcontractor",
            apiVersion = Version(request),
            params = params,
            requestBody = None
          )
        }

      requestHandler.handleRequest()
    }

}
