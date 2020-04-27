package v1.mocks.validators

import org.scalamock.handlers.CallHandler1
import v1.models.errors.MtdError
import v1.models.request.{AmendRawData}

class MockAmendValidator {

  val mockValidator: AmendValidator = mock[AmendValidator]

  object MockDeleteValidator {

    def validate(data: AmendRawData): CallHandler1[AmendRawData, List[MtdError]] = {
      (mockValidator
        .validate(_: AmendRawData))
        .expects(data)
    }
  }
}
