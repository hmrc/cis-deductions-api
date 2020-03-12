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
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.Logging
import v1.controllers.requestParsers.SampleRequestDataParser
import v1.hateoas.HateoasFactory
import v1.models.audit.{AuditEvent, SampleAuditDetail, SampleAuditResponse}
import v1.models.auth.UserDetails
import v1.models.domain.SampleHateoasData
import v1.models.errors._
import v1.models.requestData.SampleRawData
import v1.services.{SampleService, _}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SampleController @Inject()(val authService: EnrolmentsAuthService,
                                 val lookupService: MtdIdLookupService,
                                 requestDataParser: SampleRequestDataParser,
                                 sampleService: SampleService,
                                 hateoasFactory: HateoasFactory,
                                 auditService: AuditService,
                                 cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "SampleController", endpointName = "sampleEndpoint")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      val rawData = SampleRawData(nino, taxYear, request.body)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestDataParser.parseRequest(rawData))
          serviceResponse <- EitherT(sampleService.doServiceThing(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory.wrap(serviceResponse.responseData, SampleHateoasData(nino)).asRight[ErrorWrapper])
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")
          auditSubmission(createAuditDetails(rawData, CREATED, serviceResponse.correlationId, request.userDetails))

          Created(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        val result = errorResult(errorWrapper).withApiHeaders(correlationId)
        auditSubmission(createAuditDetails(rawData, result.header.status, correlationId, request.userDetails, Some(errorWrapper)))
        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case RuleIncorrectOrEmptyBodyError | BadRequestError | NinoFormatError | TaxYearFormatError | RuleTaxYearNotSupportedError |
           RuleTaxYearRangeExceededError =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def createAuditDetails(rawData: SampleRawData,
                                 statusCode: Int,
                                 correlationId: String,
                                 userDetails: UserDetails,
                                 errorWrapper: Option[ErrorWrapper] = None): SampleAuditDetail = {
    val response = errorWrapper
      .map { wrapper =>
        SampleAuditResponse(statusCode, Some(wrapper.auditErrors))
      }
      .getOrElse(SampleAuditResponse(statusCode, None))

    SampleAuditDetail(userDetails.userType, userDetails.agentReferenceNumber, rawData.nino, rawData.taxYear, correlationId, response)
  }

  private def auditSubmission(details: SampleAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("sampleAuditType", "sample-transaction-type", details)
    auditService.auditEvent(event)
  }
}
