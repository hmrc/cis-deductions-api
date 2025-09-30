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

package v2.controllers

import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers.*
import shared.hateoas.*
import shared.routing.Version
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator
import v2.controllers.validators.CreateValidatorFactory
import v2.models.response.create.CreateHateoasData
import v2.services.CreateService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CreateController @Inject() (val authService: EnrolmentsAuthService,
                                  val lookupService: MtdIdLookupService,
                                  validatorFactory: CreateValidatorFactory,
                                  service: CreateService,
                                  hateoasFactory: HateoasFactory,
                                  auditService: AuditService,
                                  cc: ControllerComponents,
                                  val idGenerator: IdGenerator)(using ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "create"

  given endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateRequestController",
      endpointName = "createEndpoint"
    )

  def create(nino: String): Action[JsValue] = authorisedAction(nino).async(parse.json) { implicit request =>
    given ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

    val validator = validatorFactory.validator(nino, request.body)
    val requestHandler = RequestHandler
      .withValidator(validator)
      .withService(service.createDeductions)
      .withAuditing(AuditHandler(
        auditService = auditService,
        auditType = "CreateCisDeductionsForSubcontractor",
        transactionName = "create-cis-deductions-for-subcontractor",
        apiVersion = Version(request),
        params = Map("nino" -> nino),
        requestBody = Some(request.body),
        includeResponse = true
      ))
      .withHateoasResultFrom(hateoasFactory) { (request, _) =>
        CreateHateoasData(nino, request)
      }

    requestHandler.handleRequest()

  }

}
