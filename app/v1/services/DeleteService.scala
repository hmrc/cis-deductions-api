package v1.services

import cats.data.EitherT
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import v1.support.DesResponseMappingSupport
import utils.Logging
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.DeleteRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteService @Inject()(connector: DeleteConnector) extends DesResponseMappingSupport with Logging {

  def deleteDeductions(request: DeleteRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.delteDeduction(request)).leftMap(mapDesErrors(desErrorMap))
    } yield desResponseWrapper
      result.value
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_IDVALUE" -> NinoFormatError,
      "NOT_FOUND" -> NotFoundError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )
}
