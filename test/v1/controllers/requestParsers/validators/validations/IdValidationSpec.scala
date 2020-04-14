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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.DeductionIdFormatError

class IdValidationSpec extends UnitSpec {

  "validate" should {
    "return no errors" when {
      "a valid id is supplied" in {

        val validId = "S4636A77V5KB8625U"
        val validationResult = IdValidation.validate(validId)
        validationResult.isEmpty shouldBe true
      }
    }

    "return an error" when {
      "when an ivalid id is supplied" in {
        val validId = "contractor1"
        val validationResult = IdValidation.validate(validId)
        validationResult.isEmpty shouldBe false
        validationResult.head shouldBe DeductionIdFormatError
      }
    }
  }
}