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

package v2.controllers

import api.controllers._
import api.models.domain.TaxYear
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers._
import v2.hateoas.HateoasFactory
import v2.models.request.retrieve.RetrieveRawData
import v2.models.response.retrieve.RetrieveHateoasData
import v2.services.RetrieveService

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

  def retrieve(nino: String, taxYear: String, source: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = RetrieveRawData(nino, taxYear, source)

      val requestHandler = RequestHandler
        .withParser(requestParser)
        .withService(service.retrieveDeductions)
        .withResultCreator(ResultCreator.hateoasListWrapping(hateoasFactory)((_, _) =>
          RetrieveHateoasData(nino, TaxYear.fromDownstream(taxYear), source)))
        .withAuditing {
          val params =
            Map("nino" -> nino, "taxYear" -> taxYear, "source" -> source)

          AuditHandler(
            auditService = auditService,
            auditType = "RetrieveCisDeductionsForSubcontractor",
            transactionName = "retrieve-cis-deductions-for-subcontractor",
            params = params,
            requestBody = None,
            includeResponse = true
          )
        }

      requestHandler.handleRequest(rawData)
    }

}
