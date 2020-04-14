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

package v1.controllers.requestParsers.validators

import support.UnitSpec
import v1.models.errors.{DeductionIdFormatError, NinoFormatError}
import v1.models.request.DeleteRawData

class DeleteValidatorSpec extends UnitSpec {

  private val validNino = "AA123456A"
  private val validId = "S4636A77V5KB8625U"

  val validator = new DeleteValidator()

  "running a delete validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(DeleteRawData(validNino, validId)) shouldBe Nil
      }
    }
    "return a single error" when {
      "an invalid nino is supplied" in {
        validator.validate(DeleteRawData("23456A", validId)) shouldBe List(NinoFormatError)
      }
      "an invalid id is supplied" in {
        validator.validate(DeleteRawData(validNino, "contractor1")) shouldBe List(DeductionIdFormatError)
      }
    }
    "return multiple errors" when {
      "multiple wrong fields are supplied" in {
        validator.validate(DeleteRawData("2sbt3456A", "idcontract123")) shouldBe List(NinoFormatError, DeductionIdFormatError)
      }
    }
  }
}
