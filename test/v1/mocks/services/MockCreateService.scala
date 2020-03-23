package v1.mocks.services

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.models.errors.ErrorWrapper
import v1.models.outcomes.ResponseWrapper
import v1.models.request.CreateRequestData
import v1.models.responseData.CreateResponseModel
import v1.services.CreateService

import scala.concurrent.{ExecutionContext, Future}

trait MockCreateService extends MockFactory {

  val mockService: CreateService = mock[CreateService]

  object MockCreateService {

    def submitCreateRequest(requestData: CreateRequestData):
    CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[CreateResponseModel]]]] = {
      (mockService
        .createDeductions(_: CreateRequestData)(_: HeaderCarrier, _: ExecutionContext, _: EndpointLogContext))
        .expects(requestData, *, *, *)
    }
  }


}
