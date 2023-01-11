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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.InvalidTaxYearParameterError
import v1.models.utils.JsonErrorValidators

class TaxYearTysParameterValidationSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    "return no errors" when {

      "a tax year that is 2023-24 is supplied" in {
        val validTaxYear     = "2023-24"
        val validationResult = TaxYearTysParameterValidation.validate(validTaxYear)
        validationResult.isEmpty shouldBe true
      }

      "the minimum allowed TYS tax year is supplied" in {
        val validTaxYear     = "2023-24"
        val validationResult = TaxYearTysParameterValidation.validate(validTaxYear)
        validationResult.isEmpty shouldBe true
      }
    }

    "return the given error" when {
      "a tax year below 2023-24 is supplied" in {
        val invalidTaxYear   = "2021-22"
        val validationResult = TaxYearTysParameterValidation.validate(invalidTaxYear)
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe InvalidTaxYearParameterError
      }
    }
  }

}
