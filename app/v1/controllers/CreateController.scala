/*
 * Copyright 2022 HM Revenue & Customs
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
import cats.implicits._
import config.AppConfig
import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.CreateRequestParser
import v1.hateoas.HateoasFactory
import v1.models.audit._
import v1.models.errors._
import v1.models.request.create.CreateRawData
import v1.models.response.create.CreateHateoasData
import v1.services.{AuditService, CreateService, EnrolmentsAuthService, MtdIdLookupService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateRequestController",
      endpointName = "createEndpoint"
    )

  def createRequest(nino: String): Action[JsValue] = authorisedAction(nino).async(parse.json) { implicit request =>
    implicit val correlationId: String = idGenerator.getCorrelationId
    logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
      s"with correlationId : $correlationId")
    val rawData = CreateRawData(nino, request.body)

    val result = for {
      parsedRequest   <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
      serviceResponse <- EitherT(service.createDeductions(parsedRequest))
      vendorResponse <- EitherT.fromEither[Future](
        hateoasFactory.wrap(serviceResponse.responseData, CreateHateoasData(nino, parsedRequest)).asRight[ErrorWrapper]
      )
    } yield {
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
          s"Success response received with CorrelationId: ${serviceResponse.correlationId}")
      auditSubmission(
        createAuditDetails(
          rawData,
          OK,
          serviceResponse.correlationId,
          request.userDetails,
          None,
          None,
          requestBody = Some(request.body),
          responseBody = Some(Json.toJson(vendorResponse))))

      Ok(Json.toJson(vendorResponse))
        .withApiHeaders(serviceResponse.correlationId)
        .as(MimeTypes.JSON)
    }

    result.leftMap { errorWrapper =>
      val resCorrelationId = errorWrapper.correlationId
      val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

      logger.warn(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
          s"Error response received with CorrelationId: $resCorrelationId")

      auditSubmission(
        createAuditDetails(
          rawData,
          result.header.status,
          correlationId,
          request.userDetails,
          None,
          Some(errorWrapper),
          requestBody = Some(request.body)))
      result
    }.merge
  }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case RuleIncorrectOrEmptyBodyError | NinoFormatError | BadRequestError | DeductionFromDateFormatError | DeductionToDateFormatError |
          FromDateFormatError | ToDateFormatError | RuleDeductionAmountError | RuleCostOfMaterialsError | RuleGrossAmountError |
          EmployerRefFormatError | RuleTaxYearNotSupportedError =>
        BadRequest(Json.toJson(errorWrapper))
      case RuleDateRangeInvalidError | RuleUnalignedDeductionsPeriodError | RuleDeductionsDateRangeInvalidError | RuleTaxYearNotEndedError |
          RuleDuplicatePeriodError | RuleDuplicateSubmissionError =>
        Forbidden(Json.toJson(errorWrapper))
      case NotFoundError   => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("CreateCisDeductionsForSubcontractor", "create-cis-deductions-for-subcontractor", details)
    auditService.auditEvent(event)
  }

}
