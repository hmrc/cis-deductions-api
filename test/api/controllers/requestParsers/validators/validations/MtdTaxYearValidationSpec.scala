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

import api.models.errors.RuleTaxYearNotSupportedError
import api.models.utils.JsonErrorValidators
import support.UnitSpec

class MtdTaxYearValidationSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    "return no errors" when {
      "a tax year greater than 2017 is supplied" in {
        val validTaxYear = "2018-19"

        val result = MtdTaxYearValidation.validate(validTaxYear, RuleTaxYearNotSupportedError)
        result.isEmpty shouldBe true

      }

      "the minimum allowed tax year is supplied" in {
        val validTaxYear = "2017-18"

        val result = MtdTaxYearValidation.validate(validTaxYear, RuleTaxYearNotSupportedError)
        result.isEmpty shouldBe true
      }

    }

    "return the given error" when {
      "a tax year below 2017 is supplied" in {
        val invalidTaxYear = "2015-16"

        val result = MtdTaxYearValidation.validate(invalidTaxYear, RuleTaxYearNotSupportedError)
        result.isEmpty shouldBe false
        result.length shouldBe 1
        result.head shouldBe RuleTaxYearNotSupportedError
      }
    }
  }

}