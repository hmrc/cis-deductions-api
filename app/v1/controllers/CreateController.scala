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

import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.controllers._
import shared.utils.IdGenerator
import v1.controllers.validators.CreateValidatorFactory
import v1.models.response.create.CreateHateoasData
import v1.services.CreateService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CreateController @Inject() (val authService: EnrolmentsAuthService,
                                  val lookupService: MtdIdLookupService,
                                  validatorFactory: CreateValidatorFactory,
                                  service: CreateService,
                                  hateoasFactory: HateoasFactory,
                                  auditService: AuditService,
                                  cc: ControllerComponents,
                                  val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateRequestController",
      endpointName = "createEndpoint"
    )

  def create(nino: String): Action[JsValue] = authorisedAction(nino).async(parse.json) { implicit request =>
//    implicit val apiVersion: Version = Version.from(request, orElse = Version1)
    implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

    val validator = validatorFactory.validator(nino, request.body)
    val requestHandler = RequestHandler
      .withValidator(validator)
      .withService(service.createDeductions)
      .withAuditing(AuditHandler(
        auditService = auditService,
        auditType = "CreateCisDeductionsForSubcontractor",
        transactionName = "create-cis-deductions-for-subcontractor",
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
