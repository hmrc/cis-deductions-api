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
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.Logging
import v1.controllers.requestParsers.AmendRequestParser
import v1.hateoas.HateoasFactory
import v1.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.auth.UserDetails
import v1.models.errors._
import v1.models.hateoas.HateoasWrapper
import v1.models.outcomes.ResponseWrapper
import v1.models.request.{AmendRawData, AmendRequestData}
import v1.models.responseData.{AmendHateoasData, AmendResponse, CreateHateoasData, CreateResponseModel}
import v1.services.{AmendService, AuditService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

class AmendController @Inject()(val authService: EnrolmentsAuthService,
                                val lookupService: MtdIdLookupService,
                                requestParser: AmendRequestParser,
                                service: AmendService,
                                auditService: AuditService,
                                hateoasFactory: HateoasFactory,
                                cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "AmendController",
      endpointName = "amendEndpoint"
    )
  def amendRequest(nino: String, id:String): Action[JsValue] = authorisedAction(nino).async(parse.json) { implicit request =>
    val rawData = AmendRawData(nino,id,request.body)
    val parseResponse: Either[ErrorWrapper, AmendRequestData] = requestParser.parseRequest(rawData)

    val serviceResponse = parseResponse match {
      case Right(data) => service.amendDeductions(data)
      case Left(errorWrapper) =>
        val futureError: Future[Either[ErrorWrapper, ResponseWrapper[AmendResponse]]] =
          Future.successful(Left(errorWrapper))
        futureError
    }
    serviceResponse.map {
      case Right(responseWrapper) =>
        val hateoasWrappedResponse: HateoasWrapper[AmendResponse] =
          hateoasFactory.wrap(responseWrapper.responseData, AmendHateoasData(nino))

        logger.info(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Success response received with CorrelationId: ${responseWrapper.correlationId}")
        auditSubmission(
          createAuditDetails(rawData, OK, responseWrapper.correlationId, request.userDetails, None, Some(Json.toJson(hateoasWrappedResponse))))

        Ok(Json.toJson(hateoasWrappedResponse)).withApiHeaders(responseWrapper.correlationId)
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
      case RuleIncorrectOrEmptyBodyError | BadRequestError | NinoFormatError | TaxYearFormatError | RuleTaxYearNotSupportedError |
           RuleTaxYearRangeExceededError | DeductionFromDateFormatError | DeductionToDateFormatError | FromDateFormatError |
           ToDateFormatError | RuleToDateBeforeFromDateError | RuleDeductionsDateRangeInvalidError | RuleDateRangeInvalidError |
           RuleDeductionAmountError | RuleCostOfMaterialsError | RuleGrossAmountError | RuleFromDateError | RuleToDateError | DeductionIdFormatError =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def createAuditDetails(rawData: AmendRawData,
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
    val event = AuditEvent("amendCisDeductionsAuditType", "amend-cis-deductions-transaction-type", details)
    auditService.auditEvent(event)
  }


}



