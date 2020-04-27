package v1.controllers.requestParsers

import junit.framework.Test
import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v1.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError}
import v1.models.request.{AmendRawData, AmendRequestData, CreateRawData}

class AmendRequestModelParserSpec extends UnitSpec{

  val nino = "AA123456A"
  val invalidNino = "PLKL87654"
  val id = "S4636A77V5KB8625U"

  "parser" should {
    "accept a valid input" when {
      "a cis deduction has been passed" in new Test {
        val inputData = AmendRawData(nino, id, requestJson)

        MockValidator
          .validate(inputData)
          .returns(Nil)

        private val result = parser.parseRequest(inputData)

      }

      "Missing option field has passed" in new Test {
        val inputData = AmendRawData(nino, id, requestJson)

        MockValidator
          .validate(inputData)
          .returns(Nil)

        private val result = parser.parseRequest(inputData)
      }

      "Reject invalid input" when {
        "mandatory field is given invalid data" in new Test {
          val inputData = AmendRawData(nino, id, invalidRequestJson)

          MockValidator
            .validate(inputData)
            .returns(List(BadRequestError))

          private val result = parser.parseRequest(inputData)
          result shouldBe Left(ErrorWrapper(None,List(BadRequestError)))
        }
        "Nino format is incorrect" in new Test {
          val inputData = AmendRawData(nino, id, requestJson)

          MockValidator
            .validate(inputData)
            .returns(List(NinoFormatError))

          private val result = parser.parseRequest(inputData)
          result shouldBe Left(ErrorWrapper(None,List(NinoFormatError)))
        }
        "Id format is incorrect" in new Test {
          val inputData = AmendRawData(nino, "id", requestJson)

          MockValidator
            .validate(inputData)
            .returns(List(NinoFormatError))

          private val result = parser.parseRequest(inputData)
          result shouldBe Left(ErrorWrapper(None,List(NinoFormatError)))
        }
      }
    }
  }
}
