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
import config.AppConfig
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import routing.{Version, Version1}
import shared.controllers._
import shared.utils.{IdGenerator, Logging}
import v1.controllers.validators.RetrieveValidatorFactory
import v1.models.response.retrieve.RetrieveHateoasData
import v1.services.RetrieveService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RetrieveController @Inject() (val authService: EnrolmentsAuthService,
                                    val lookupService: MtdIdLookupService,
                                    validatorFactory: RetrieveValidatorFactory,
                                    service: RetrieveService,
                                    auditService: AuditService,
                                    hateoasFactory: HateoasFactory,
                                    cc: ControllerComponents,
                                    val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc)
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveController",
      endpointName = "retrieveEndpoint"
    )

  def retrieve(nino: String, fromDate: Option[String], toDate: Option[String], source: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val apiVersion: Version = Version.from(request, orElse = Version1)
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, fromDate, toDate, source)
      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.retrieveDeductions)
        .withResultCreator(ResultCreator.hateoasListWrapping(hateoasFactory)((request, _) =>
          RetrieveHateoasData(nino, request.fromDate, request.toDate, source, request.taxYear)))
        .withAuditing {
          val params =
            Map("nino" -> nino) ++
              fromDate.map(x => "fromDate" -> x) ++
              toDate.map(x => "toDate" -> x) ++
              source.map(x => "source" -> x)

          AuditHandler(
            auditService = auditService,
            auditType = "RetrieveCisDeductionsForSubcontractor",
            transactionName = "retrieve-cis-deductions-for-subcontractor",
            params = params,
            requestBody = None,
            includeResponse = true
          )
        }

      requestHandler.handleRequest()
    }

}
