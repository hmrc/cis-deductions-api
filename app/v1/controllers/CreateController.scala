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
import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.{AppConfig, FeatureSwitches}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.CreateRequestParser
import v1.models.request.create.CreateRawData
import v1.models.response.create.CreateHateoasData
import v1.services.CreateService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CreateController @Inject() (val authService: EnrolmentsAuthService,
                                  val lookupService: MtdIdLookupService,
                                  requestParser: CreateRequestParser,
                                  service: CreateService,
                                  hateoasFactory: HateoasFactory,
                                  auditService: AuditService,
                                  appConfig: AppConfig,
                                  cc: ControllerComponents,
                                  val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateRequestController",
      endpointName = "createEndpoint"
    )

  def create(nino: String): Action[JsValue] = authorisedAction(nino).async(parse.json) { implicit request =>
    implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

    val rawData = CreateRawData(
      nino = nino,
      body = request.body,
      temporalValidationEnabled = FeatureSwitches()(appConfig).isTemporalValidationEnabled
    )

    val requestHandler = RequestHandler
      .withParser(requestParser)
      .withService(service.createDeductions)
      .withAuditing(AuditHandler(
        auditService = auditService,
        auditType = "CreateCisDeductionsForSubcontractor",
        transactionName = "create-cis-deductions-for-subcontractor",
        pathParams = Map("nino" -> nino),
        requestBody = Some(request.body),
        includeResponse = true
      ))
      .withHateoasResultFrom(hateoasFactory) { (request, _) =>
        CreateHateoasData(nino, request)
      }

    requestHandler.handleRequest(rawData)

  }

}
