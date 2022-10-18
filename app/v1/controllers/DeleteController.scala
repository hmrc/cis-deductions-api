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
import play.api.http.MimeTypes
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.DeleteRequestParser
import v1.models.audit.{AuditEvent, GenericAuditDetail}
import v1.models.errors._
import v1.models.request.delete.DeleteRawData
import v1.services.{AuditService, DeleteService, EnrolmentsAuthService, MtdIdLookupService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteController @Inject() (val authService: EnrolmentsAuthService,
                                  val lookupService: MtdIdLookupService,
                                  requestParser: DeleteRequestParser,
                                  service: DeleteService,
                                  auditService: AuditService,
                                  cc: ControllerComponents,
                                  val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "DeleteController",
      endpointName = "deleteEndpoint"
    )

  def deleteRequest(nino: String, submissionId: String, taxYear: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"with correlationId : $correlationId")
      val rawData = DeleteRawData(nino, submissionId, taxYear)

      val result = for {
        parsedRequest   <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
        serviceResponse <- EitherT(service.deleteDeductions(parsedRequest))
      } yield {
        logger.info(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Success response received with CorrelationId: ${serviceResponse.correlationId}")
        auditSubmission(
          createAuditDetails(
            rawData,
            NO_CONTENT,
            serviceResponse.correlationId,
            request.userDetails,
            Some(rawData.submissionId),
            None,
            responseBody = Some(Json.toJson(serviceResponse.correlationId))
          ))

        NoContent
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
          createAuditDetails(rawData, result.header.status, correlationId, request.userDetails, Some(rawData.submissionId), Some(errorWrapper)))
        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {

    errorWrapper.error match {
      case _
          if errorWrapper.containsAnyOf(
            BadRequestError,
            NinoFormatError,
            SubmissionIdFormatError,
            TaxYearFormatError,
            RuleTaxYearNotSupportedError,
            InvalidTaxYearParameterError
          ) =>
        BadRequest(Json.toJson(errorWrapper))

      case NotFoundError           => NotFound(Json.toJson(errorWrapper))
      case StandardDownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _                       => unhandledError(errorWrapper)
    }
  }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("DeleteCisDeductionsForSubcontractor", "delete-cis-deductions-for-subcontractor", details)
    auditService.auditEvent(event)
  }

}
