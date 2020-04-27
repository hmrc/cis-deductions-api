package v1.controllers.requestParsers

import javax.inject.Inject
import uk.gov.hmrc.domain.Nino
import v1.models.request.{AmendRawData, AmendRequest, AmendRequestData}

class AmendRequestModelParser@Inject(validators: )
  extends RequestParser[AmendRawData, AmendRequestData]{

  override protected def requestFor(data: AmendRawData): AmendRequestData = {
    val requestBody = data.body.as[AmendRequest]
    AmendRequestData(Nino(data.nino), data.id, requestBody)
  }
}
