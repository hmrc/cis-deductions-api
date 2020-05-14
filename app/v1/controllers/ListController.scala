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

import javax.inject.Inject
import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.Logging
import v1.controllers.requestParsers._
import v1.hateoas.HateoasFactory
import v1.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.auth.UserDetails
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.{ListDeductionsRawData, ListDeductionsRequest}
import v1.models.responseData.listDeductions.{DeductionsDetails, ListResponseHateoasData, ListResponseModel}
import v1.services.{AuditService, EnrolmentsAuthService, ListService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

class ListController @Inject()(val authService: EnrolmentsAuthService,
                                  val lookupService: MtdIdLookupService,
                                  requestParser: ListDeductionRequestParser,
                                  service: ListService,
                                  auditService: AuditService,
                                  hateoasFactory: HateoasFactory,
                                  cc: ControllerComponents)
                                 (implicit ec: ExecutionContext)
extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "ListController",
      endpointName = "listEndpoint"
    )

  def listDeductions(nino: String, fromDate: Option[String], toDate: Option[String], source: Option[String]) : Action[AnyContent] =
  authorisedAction(nino).async { implicit request =>
    val rawData = ListDeductionsRawData(nino,fromDate,toDate,source)
    val parseResponse: Either[ErrorWrapper, ListDeductionsRequest] = requestParser.parseRequest(rawData)
    val serviceResponse = parseResponse match {
      case Right(data) =>
        service.listDeductions(data)
      case Left(errorWrapper) =>
        val futureError: Future[Either[ErrorWrapper, ResponseWrapper[ListResponseModel[DeductionsDetails]]]] =
          Future.successful(Left(errorWrapper))
        futureError
    }
      serviceResponse.map {
      case Right(responseWrapper) =>
        logger.info(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Success response received with CorrelationId: ${responseWrapper.correlationId}")

        val hateoasResponse = hateoasFactory.wrapList(responseWrapper.responseData,
          ListResponseHateoasData(nino, fromDate.getOrElse(""), toDate.getOrElse(""), source, responseWrapper.responseData))

        auditSubmission(
          createAuditDetails(rawData, OK, responseWrapper.correlationId, request.userDetails, None, Some(Json.toJson(hateoasResponse))))

        Ok(Json.toJson(hateoasResponse))
          .withApiHeaders(responseWrapper.correlationId)
          .as(MimeTypes.JSON)

      case Left(errorWrapper) =>
        val correlationId = getCorrelationId(errorWrapper)
        val result = errorResult(errorWrapper).withApiHeaders(correlationId)
        auditSubmission(createAuditDetails(rawData, result.header.status, correlationId, request.userDetails, Some(errorWrapper)))
        result
    }
  }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.errors.head: @unchecked) match {
      case BadRequestError | NinoFormatError | RuleDateRangeInvalidError | FromDateFormatError | RuleMissingFromDateError |
           ToDateFormatError | RuleMissingToDateError | RuleSourceError | UnauthorisedError | RuleToDateError | RuleFromDateError =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def createAuditDetails(rawData: ListDeductionsRawData,
                                 statusCode: Int,
                                 correlationId: String,
                                 userDetails: UserDetails,
                                 errorWrapper: Option[ErrorWrapper] = None,
                                 responseBody: Option[JsValue] = None): GenericAuditDetail = {
    val response = errorWrapper
      .map { wrapper =>
        AuditResponse(statusCode, Some(wrapper.auditErrors), None)
      }
      .getOrElse(AuditResponse(statusCode, None, responseBody ))

    GenericAuditDetail(userDetails.userType, userDetails.agentReferenceNumber, rawData.nino, correlationId, response)
  }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("listCisDeductionsAuditType", "list-cis-deductions-transaction-type", details)
    auditService.auditEvent(event)
  }
}
