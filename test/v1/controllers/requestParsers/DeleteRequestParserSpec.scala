/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.controllers.requestParsers

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v1.mocks.validators.MockDeleteValidator
import v1.models.errors.{BadRequestError, DeductionIdFormatError, ErrorWrapper, NinoFormatError}
import v1.models.request.{DeleteRawData, DeleteRequest}

class DeleteRequestParserSpec extends UnitSpec {
  val nino = "AA123456B"
  val id = "S4636A77V5KB8625U"

  val   inputData = DeleteRawData(nino, id)

  trait Test extends MockDeleteValidator {
    lazy val parser = new DeleteRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid raw data is supplied" in new Test {
        MockDeleteValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe
          Right(DeleteRequest(Nino(nino), id))
      }
    }
    "return an error wrapper" when {
      "a single validation error occurs" in new Test {
        MockDeleteValidator.validate(inputData).returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(None, Seq(NinoFormatError)))
      }
    }
    "multiple validation errors occur" in new Test {
      MockDeleteValidator.validate(inputData).returns(List(NinoFormatError, DeductionIdFormatError))

      parser.parseRequest(inputData) shouldBe
        Left(ErrorWrapper(None ,Seq(BadRequestError, NinoFormatError, DeductionIdFormatError)))
    }
  }
}