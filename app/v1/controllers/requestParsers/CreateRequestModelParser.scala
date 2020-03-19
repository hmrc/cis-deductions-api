package v1.controllers.requestParsers

import javax.inject.Inject
import uk.gov.hmrc.domain.Nino
import v1.controllers.requestParsers.validators.CreateRequestModelValidator
import v1.models.request.{CreateRawData, CreateRequestData, CreateRequestModel}
import v1.models.requestData.{SampleRawData, SampleRequestData}

class CreateRequestModelParser @Inject()(val validator: CreateRequestModelValidator)
  extends RequestParser[CreateRawData, CreateRequestData] {

  override protected def requestFor(data: CreateRawData): CreateRequestData = {
    val requestBody = data.body.as[CreateRequestModel]

CreateRequestData(Nino(data.nino), requestBody)

  }

}
