/*
 * Copyright 2023 HM Revenue & Customs
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

package api.controllers.requestParsers.validators.validations

import api.models.errors.RuleSourceInvalidError
import support.UnitSpec

class SourceValidationSpec extends UnitSpec {

  "validate" should {
    "return no errors" when {
      "when source all is supplied" in {
        val validationResult = SourceValidation.validate("all")
        validationResult.isEmpty shouldBe true
      }
      "when source contractor supplied" in {
        val validationResult = SourceValidation.validate("contractor")
        validationResult.isEmpty shouldBe true
      }
      "when source customer supplied" in {
        val validationResult = SourceValidation.validate("customer")
        validationResult.isEmpty shouldBe true
      }
      "when no source is supplied" in {
        val validationResult = SourceValidation.validate(None)
        validationResult.isEmpty shouldBe true
      }
      "when some valid source is supplied" in {
        val validationResult = SourceValidation.validate(Some("customer"))
        validationResult.isEmpty shouldBe true
      }
    }

    "return an error" when {
      "when an invalid source format is supplied" in {
        val validationResult = SourceValidation.validate("invalid")
        validationResult shouldBe List(RuleSourceInvalidError)
      }
      "when some invalid source is supplied" in {
        val validationResult = SourceValidation.validate(Some("invalid"))
        validationResult shouldBe List(RuleSourceInvalidError)
      }
    }
  }

}
