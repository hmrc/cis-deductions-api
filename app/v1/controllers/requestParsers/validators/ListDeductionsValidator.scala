package v1.controllers.requestParsers.validators

import config.FixedConfig
import v1.controllers.requestParsers.validators.validations.{NinoValidation, SourceValidation}
import v1.models.errors.MtdError
import v1.models.request.ListDeductionsRawData

class ListDeductionsValidator extends Validator[ListDeductionsRawData] with FixedConfig{

  private val validationSet = List(parameterFormatValidation)

  private def parameterFormatValidation: ListDeductionsRawData => List[List[MtdError]] = (data: ListDeductionsRawData) => List(
    NinoValidation.validate(data.nino),
    SourceValidation.validate(data.source),
  )

  override def validate(data: ListDeductionsRawData): List[MtdError] = run(validationSet, data).distinct
}
