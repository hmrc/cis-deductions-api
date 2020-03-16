package v1.models.requestData

import uk.gov.hmrc.domain.Nino

case class CreateCisDeductionsRequestData(nino: Nino, body: CreateCisDeductionsRequestModel)
