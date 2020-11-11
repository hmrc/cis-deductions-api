/*
 * Copyright 2020 HM Revenue & Customs
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

import cats.data.EitherT
import config.AppConfig
import javax.inject.Inject
import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers._
import v1.hateoas.HateoasFactory
import v1.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.auth.UserDetails
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.{RetrieveRawData, RetrieveRequestData}
import v1.models.responseData.{CisDeductions, CreateHateoasData, RetrieveResponseHateoasData, RetrieveResponseModel}
import v1.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService, RetrieveService}
import cats.implicits._

import scala.concurrent.{ExecutionContext, Future}

class RetrieveController @Inject()(val authService: EnrolmentsAuthService,
                                   val lookupService: MtdIdLookupService,
                                   requestParser: RetrieveRequestParser,
                                   service: RetrieveService,
                                   auditService: AuditService,
                                   hateoasFactory: HateoasFactory,
                                   appConfig: AppConfig,
                                   cc: ControllerComponents,
                                   val idGenerator: IdGenerator)
                                  (implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveController",
      endpointName = "retrieveEndpoint"
    )

  def retrieveDeductions(nino: String, fromDate: Option[String], toDate: Option[String], source: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"with correlationId : $correlationId")
      val rawData = RetrieveRawData(nino, fromDate, toDate, source)

      val result = for {
        parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
        serviceResponse <- EitherT(service.retrieveDeductions(parsedRequest))
        vendorResponse <- EitherT.fromEither[Future](
          hateoasFactory.wrapList(serviceResponse.responseData,
            RetrieveResponseHateoasData(nino, fromDate.getOrElse(""), toDate.getOrElse(""), source, serviceResponse.responseData)).asRight[ErrorWrapper]
        )
      } yield {
        logger.info(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

        auditSubmission(
          createAuditDetails(rawData, OK, serviceResponse.correlationId, request.userDetails, None, responseBody = Some(Json.toJson(vendorResponse))))

        Ok(Json.toJson(vendorResponse))
          .withApiHeaders(serviceResponse.correlationId)
          .as(MimeTypes.JSON)
      }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(createAuditDetails(rawData, result.header.status, correlationId, request.userDetails, Some(errorWrapper)))
        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.errors.head: @unchecked) match {
      case BadRequestError | NinoFormatError | FromDateFormatError | RuleMissingFromDateError | ToDateFormatError
           | RuleMissingToDateError | RuleSourceError | RuleTaxYearNotSupportedError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case RuleDateRangeOutOfDate | RuleDateRangeInvalidError => Forbidden(Json.toJson(errorWrapper))
    }
  }

  private def createAuditDetails(rawData: RetrieveRawData,
                                 statusCode: Int,
                                 correlationId: String,
                                 userDetails: UserDetails,
                                 errorWrapper: Option[ErrorWrapper] = None,
                                 requestBody: Option[JsValue] = None,
                                 responseBody: Option[JsValue] = None): GenericAuditDetail = {
    val response = errorWrapper
      .map { wrapper =>
        AuditResponse(statusCode, Some(wrapper.auditErrors), None)
      }
      .getOrElse(AuditResponse(statusCode, None, responseBody))

    GenericAuditDetail(userDetails.userType, userDetails.agentReferenceNumber, rawData.nino, None, correlationId, requestBody, response)
  }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("RetrieveCisDeductionsForSubcontractor", "retrieve-cis-deductions-for-subcontractor", details)
    auditService.auditEvent(event)
  }
}
