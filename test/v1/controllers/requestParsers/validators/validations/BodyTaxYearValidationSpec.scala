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
import v1.models.errors.{RuleFromDateError, RuleToDateError}
import v1.models.utils.JsonErrorValidators
import v1.fixtures.CreateRequestFixtures._

class BodyTaxYearValidationSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    "return no errors" when {
      "a request body with valid toDate" in {
        val validationResult = BodyTaxYearValidation.validate("2019-04-05", "toDate", RuleToDateError)
        validationResult.isEmpty shouldBe true
      }
      "a request body with valid fromDate" in {
        val validationResult = BodyTaxYearValidation.validate("2019-04-06", "fromDate", RuleFromDateError)
        validationResult.isEmpty shouldBe true
      }
    }
    "return errors" when {
      "a request body with invalid toDate" in {
        val validationResult = BodyTaxYearValidation.validate("2019-04-06", "toDate", RuleToDateError)
        validationResult shouldBe List(RuleToDateError)
      }
      "a request body with invalid fromDate" in {
        val validationResult = BodyTaxYearValidation.validate("2019-04-05", "fromDate", RuleFromDateError)
        validationResult shouldBe List(RuleFromDateError)
      }
    }
  }
}
