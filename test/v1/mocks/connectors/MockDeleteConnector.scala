package v1.mocks.connectors

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.DesOutcome
import v1.models.request.DeleteRequest

import scala.concurrent.{ExecutionContext, Future}

class MockDeleteConnector extends MockFactory{

  val mockDeleteConnector: DeleteConnector = mock[DeleteConnector]

  object MockDeleteConnector {

    def deleteDeduction(requestData: DeleteRequest):
    CallHandler[Future[DesOutcome[Unit]]] = {
      (mockDeleteConnector
        .delete(_: DeleteRequest)(_: HeaderCarrier, _: ExecutionContext))
        .expects(requestData, *, *)
    }
  }
}
