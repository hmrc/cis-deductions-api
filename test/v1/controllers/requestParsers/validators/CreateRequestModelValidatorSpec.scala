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
import v1.models.request._
import v1.fixtures.RequestFixtures._
import v1.models.errors._

class CreateRequestModelValidatorSpec extends UnitSpec{

  val nino = "AA123456A"
  val invalidNino = "GHFG197854"

  class SetUp {
    val validator = new CreateRequestModelValidator
  }
  "running validation" should {
    "return no errors" when {
      "all the fields are submitted in a request" in new SetUp {

        validator
          .validate(
            CreateRawData(nino, requestJson))
          .isEmpty shouldBe true
      }

      "an optional field is omitted in a request" in new SetUp {

        validator
          .validate(
            CreateRawData(nino, missingOptionalRequestJson))
          .isEmpty shouldBe true
      }
    }
    "return errors" when {
      "invalid body type error" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, missingMandatoryFieldRequestJson)
        )
        result.length shouldBe 1
        result shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "invalid nino is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(invalidNino,requestJson)
        )
        result.length shouldBe 1
        result shouldBe List(NinoFormatError)
      }
      "invalid fromDate format is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidFromDateFormatRequestJson)
        )
        result.length shouldBe 1
        result shouldBe List(FromDateFormatError)
      }
      "invalid toDate format is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidToDateFormatRequestJson)
        )
        result.length shouldBe 1
        result shouldBe List(ToDateFormatError)
      }
      "invalid Deduction fromDate format is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidDeductionFromDateFormatRequestJson)
        )
        result.length shouldBe 1
        result shouldBe List(DeductionFromDateFormatError)
      }
      "invalid Deduction toDate format is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidDeductionToDateFormatRequestJson)
        )
        result.length shouldBe 1
        result shouldBe List(DeductionToDateFormatError)
      }
      "invalid deductionAmount too high is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidDeductionAmountTooHighRequestJson)
        )
        result.length shouldBe 1
        result shouldBe List(RuleDeductionAmountError)
      }
      "invalid deductionAmount negative is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidDeductionAmountNegativeRequestJson)
        )
        result.length shouldBe 1
        result shouldBe List(RuleDeductionAmountError)
      }
      "invalid CostOfMaterials too high is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidCostOfMaterialsTooHighRequestJson)
        )
        result.length shouldBe 1
        result shouldBe List(RuleCostOfMaterialsError)
      }
      "invalid CostOfMaterials negative is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidCostOfMaterialsNegativeRequestJson)
        )
        result.length shouldBe 1
        result shouldBe List(RuleCostOfMaterialsError)
      }
      "invalid GrossAmount too high is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidGrossAmountTooHighRequestJson)
        )
        result.length shouldBe 1
        result shouldBe List(RuleGrossAmountError)
      }
      "invalid GrossAmount negative is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidGrossAmountNegativeRequestJson)
        )
        result.length shouldBe 1
        result shouldBe List(RuleGrossAmountError)
      }
      "invalid toDate before fromDate is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidToDateBeforeFromDateRequestJson)
        )
        result.length shouldBe 1
        result shouldBe List(RuleDateRangeInvalidError)
      }
    }
  }
}