package v1.services

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.models.outcomes.ResponseWrapper
import v1.models.request.DeleteRequest

import scala.concurrent.Future

class DeleteServiceSpec extends UnitSpec {

  val nino = Nino("AA123456A")
  val validId = "S4636A77V5KB8625U"

  val requestData = DeleteRequest(nino, validId)


  trait Test extends MockDeleteConnector {
    implicit val hc: HeaderCarrier = HederCarrier()
    implicit val logContext: EndpointLogContext = EndpoingLogContext("c", "ep")

    val service = new DeleteService(
      connector = mockDeleteConnector
    )
  }

  "service" should {
    "return a mapped result" when {
      "a service call is successful" in new Test {
        MockDeleteConnector.deleteDeduction(requestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

        await(service.deleteDeductions(requestData)) shouldBe Right(ResponseWrapper("resultId", ()))
      }
    }
  }
}
