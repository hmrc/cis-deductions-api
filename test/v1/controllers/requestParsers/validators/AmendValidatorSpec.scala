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
import v1.fixtures.AmendRequestFixtures._
import v1.models.errors._
import v1.models.request.amend.AmendRawData

class AmendValidatorSpec extends UnitSpec {

  private val validNino = "AA123456A"
  private val validId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val validator = new AmendValidator()

  "running amend validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(AmendRawData(validNino, validId, requestJson)) shouldBe Nil
      }
    }
    "return a single error" when {
      "an invalid nino is supplied" in {
        validator.validate(AmendRawData("23456A", validId, requestJson)) shouldBe List(NinoFormatError)
      }
      "an invalid submission id is supplied" in {
        validator.validate(AmendRawData(validNino, "contractor1", requestJson)) shouldBe List(SubmissionIdFormatError)
      }
    }
    "return multiple errors" when {
      "multiple wrong fields are supplied" in {
        validator.validate(AmendRawData("2sbt3456A", "idcontract123", requestJson)) shouldBe List(NinoFormatError, SubmissionIdFormatError)
      }
    }
    "return a single error" when {
      "invalid body type error" in new AmendValidator {
        private val result = validator.validate(AmendRawData(validNino, validId, missingMandatoryFieldRequestJson))
        result shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "invalid request body Deduction fromDate format is provided" in new AmendValidator {
        private val result = validator.validate(AmendRawData(validNino, validId, invalidDeductionFromDateFormatRequestJson))
        result shouldBe List(DeductionFromDateFormatError)
      }
      "invalid request body Deduction toDate format is provided" in new AmendValidator {
        private val result = validator.validate(AmendRawData(validNino, validId, invalidDeductionToDateFormatRequestJson))
        result shouldBe List(DeductionToDateFormatError)
      }
      "invalid request body deductionAmount too high is provided" in new AmendValidator {
        private val result = validator.validate(AmendRawData(validNino, validId, invalidDeductionAmountTooHighRequestJson))
        result shouldBe List(RuleDeductionAmountError)
      }
      "invalid request body deductionAmount negative is provided" in new AmendValidator {
        private val result = validator.validate(AmendRawData(validNino, validId, invalidDeductionAmountNegativeRequestJson))
        result shouldBe List(RuleDeductionAmountError)
      }
      "invalid request body CostOfMaterials too high is provided" in new AmendValidator {
        private val result = validator.validate(AmendRawData(validNino, validId, invalidCostOfMaterialsTooHighRequestJson))
        result shouldBe List(RuleCostOfMaterialsError)
      }
      "invalid request body CostOfMaterials negative is provided" in new AmendValidator {
        private val result = validator.validate(AmendRawData(validNino, validId, invalidCostOfMaterialsNegativeRequestJson))
        result shouldBe List(RuleCostOfMaterialsError)
      }
      "invalid request body GrossAmount too high is provided" in new AmendValidator {
        private val result = validator.validate(AmendRawData(validNino, validId, invalidGrossAmountTooHighRequestJson))
        result shouldBe List(RuleGrossAmountError)
      }
      "invalid request body GrossAmount negative is provided" in new AmendValidator {
        private val result = validator.validate(AmendRawData(validNino, validId, invalidGrossAmountNegativeRequestJson))
        result shouldBe List(RuleGrossAmountError)
      }
    }
  }
}
