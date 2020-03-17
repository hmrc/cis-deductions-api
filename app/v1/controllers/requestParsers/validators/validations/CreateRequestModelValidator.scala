package v1.controllers.requestParsers.validators.validations

import config.FixedConfig
import v1.controllers.requestParsers.validators.Validator
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import v1.models.request.{CreateRawData, CreateRequestData, CreateRequestModel}
import v1.models.requestData.SampleRequestData

class CreateRequestModelValidator extends Validator[CreateRawData] with FixedConfig{
private val validationSet = List(
  parameterFormatValidator,
  bodyFormatValidator

)
private def parameterFormatValidator: CreateRawData => List[List[MtdError]] = { data =>

  List(
    NinoValidation.validate(data.nino)
  )

}


  private def bodyFormatValidator: CreateRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateRequestModel](data.body.json, RuleIncorrectOrEmptyBodyError)
    )
  }

  override def validate(data: CreateRawData): List[MtdError] = run(validationSet, data)
}
