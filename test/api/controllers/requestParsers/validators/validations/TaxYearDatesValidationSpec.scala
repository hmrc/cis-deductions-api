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

import api.models.errors.RuleDateRangeInvalidError
import api.models.utils.JsonErrorValidators
import support.UnitSpec

class TaxYearDatesValidationSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    "return no errors" when {
      "a request body with valid toDate & fromDate" in {
        val validationResult = TaxYearDatesValidation.validate("2019-04-06", "2020-04-05", 1)
        validationResult.isEmpty shouldBe true
      }
    }
    "return errors" when {
      "a request body with invalid toDate" in {
        val validationResult = TaxYearDatesValidation.validate("2019-04-06", "2020-04-06", 1)
        validationResult shouldBe List(RuleDateRangeInvalidError)
      }
      "a request body with invalid fromDate" in {
        val validationResult = TaxYearDatesValidation.validate("2019-04-07", "2020-04-05", 1)
        validationResult shouldBe List(RuleDateRangeInvalidError)
      }
      "a request body with invalid date range" in {
        val validationResult = TaxYearDatesValidation.validate("2018-04-06", "2020-04-05", 1)
        validationResult shouldBe List(RuleDateRangeInvalidError)
      }
    }
  }

}
