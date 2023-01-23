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

import api.controllers.RequestContextImplicits.toCorrelationId
import api.controllers.{AuthorisedController, BaseController, EndpointLogContext, RequestContext, RequestHandler, ResultCreator}
import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import cats.data.EitherT
import cats.implicits._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers._
import v1.models.request.retrieve.RetrieveRawData
import v1.models.response.retrieve.{RetrieveHateoasData, RetrieveResponseModel}
import v1.services.RetrieveService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrieveController @Inject() (val authService: EnrolmentsAuthService,
                                    val lookupService: MtdIdLookupService,
                                    requestParser: RetrieveRequestParser,
                                    service: RetrieveService,
                                    auditService: AuditService,
                                    hateoasFactory: HateoasFactory,
                                    cc: ControllerComponents,
                                    val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveController",
      endpointName = "retrieveEndpoint"
    )

  def retrieveDeductions(nino: String, fromDate: Option[String], toDate: Option[String], source: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = RetrieveRawData(nino, fromDate, toDate, source)

      /*val hateoasData = for {
        parsedRequest   <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
        serviceResponse <- EitherT(service.retrieveDeductions(parsedRequest))
      } yield {
        RetrieveHateoasData(
          nino,
          parsedRequest.fromDate,
          parsedRequest.toDate,
          source,
          parsedRequest.taxYear,
          serviceResponse.responseData
        )
      }*/

      val requestHandler = RequestHandler
        .withParser(requestParser)
        .withService(service.retrieveDeductions)
        .withHateoasResult(hateoasFactory)(RetrieveHateoasData())
        // .withHateoasResult(hateoasFactory)(hateoasData)
        .withAuditing()

      requestHandler.handleRequest(rawData)
    }

}
