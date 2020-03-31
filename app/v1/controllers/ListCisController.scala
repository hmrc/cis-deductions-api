package v1.controllers

import cats.data.EitherT
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.Logging
import v1.controllers.requestParsers.CreateRequestModelParser
import v1.hateoas.HateoasFactory
import v1.models.audit.{AuditEvent, CreateAuditDetail}
import v1.models.errors._
import v1.models.request.ListDeductionsRawData
import v1.services.{AuditService, EnrolmentsAuthService, ListService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

//Remember to change request paeser to list request parser
class ListCisController @Inject()(val authService: EnrolmentsAuthService,
                                  val lookupService: MtdIdLookupService,
                                  requestParser: CreateRequestModelParser,
                                  service: ListService,
                                  hateoasFactory: HateoasFactory,
                                  auditService: AuditService,
                                  cc: ControllerComponents)
                                 (implicit ec: ExecutionContext)
extends AuthorisedController(cc)
with BaseController
  with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "ListCisController",
      endpointName = "listCis"
    )

def listCis(nino: String, fromDate: String, toDate: String, source: Option[String]): Action[AnyContent] =
  authorisedAction(nino).async { implicit request =>

    val rawData = ListDeductionsRawData(nino,fromDate,toDate,source)
    val result =
      for {
        parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
        response <- EitherT(service.listDeductions(parsedRequest))
      } yield {
        logger.info(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Success response received with correlationId: ${response.correlationId}"
        )
      }
    result.leftMap {errorWrapper => }
    val correlationId = getCorrelationId(errorWrapper)
    val result = errorResult(errorWrapper).withApiHeaders(correlationId)


  }

  private def errorResult(errorWrapper: ErrorWrapper) = {

    (errorWrapper.errors.head: @unchecked) match {
      case RuleIncorrectOrEmptyBodyError | BadRequestError | NinoFormatError | TaxYearFormatError | RuleTaxYearNotSupportedError |
           RuleTaxYearRangeExceededError | DeductionFromDateFormatError | DeductionToDateFormatError | FromDateFormatError |
           ToDateFormatError | RuleToDateBeforeFromDateError | RuleDeductionsDateRangeInvalidError | RuleDateRangeInvalidError |
           RuleDeductionAmountError | RuleCostOfMaterialsError | RuleGrossAmountError =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def auditSubmission(details: CreateAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("createCisDeductionsAuditType", "create-cis-deductions-transaction-type", details)
    auditService.auditEvent(event)
  }



}

