package v1.mocks.requestParsers

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v1.controllers.requestParsers.CreateRequestModelParser
import v1.models.errors.ErrorWrapper
import v1.models.request.{CreateRawData, CreateRequestData, CreateRequestModel}

trait MockCreateRequestParser extends MockFactory {

  val mockRequestDataParser: CreateRequestModelParser = mock[CreateRequestModelParser]

  object MockCreateRequestDataParser {
    def parse(data: CreateRawData): CallHandler[Either[ErrorWrapper, CreateRequestData]] = {
      (mockRequestDataParser.parseRequest(_: CreateRawData)).expects(data)
    }
  }

}
