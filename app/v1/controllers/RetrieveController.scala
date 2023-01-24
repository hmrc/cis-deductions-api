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

import api.controllers.{AuditHandler, AuthorisedController, EndpointLogContext, RequestContext, RequestHandler, ResultCreator}
import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers._
import v1.models.request.retrieve.RetrieveRawData
import v1.models.response.retrieve.RetrieveHateoasData
import v1.services.RetrieveService
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RetrieveController @Inject() (val authService: EnrolmentsAuthService,
                                    val lookupService: MtdIdLookupService,
                                    requestParser: RetrieveRequestParser,
                                    service: RetrieveService,
                                    auditService: AuditService,
                                    hateoasFactory: HateoasFactory,
                                    cc: ControllerComponents,
                                    val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveController",
      endpointName = "retrieveEndpoint"
    )

  def retrieve(nino: String, fromDate: Option[String], toDate: Option[String], source: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = RetrieveRawData(nino, fromDate, toDate, source)

      val requestHandler = RequestHandler
        .withParser(requestParser)
        .withService(service.retrieveDeductions)
        .withResultCreator(ResultCreator.hateoasListWrapping(hateoasFactory)((request, _) =>
          RetrieveHateoasData(nino, request.fromDate, request.toDate, source, request.taxYear)))
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "RetrieveCisDeductionsForSubcontractor",
          transactionName = "retrieve-cis-deductions-for-subcontractor",
          pathParams = Map("nino" -> nino),
          queryParams = Some(Map("fromDate" -> fromDate, "toDate" -> toDate, "source" -> source)),
          requestBody = None,
          includeResponse = true
        ))

      requestHandler.handleRequest(rawData)
    }

}
