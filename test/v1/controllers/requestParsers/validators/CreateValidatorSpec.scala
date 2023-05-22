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

package v1.controllers.requestParsers.validators

import api.models.errors._
import mocks.MockAppConfig
import org.joda.time.DateTime
import support.UnitSpec
import v1.fixtures.CreateRequestFixtures._
import v1.models.request.create.CreateRawData

import java.time.Year

class CreateValidatorSpec extends UnitSpec {

  val nino        = "AA123456A"
  val invalidNino = "GHFG197854"

  val now                 = DateTime.now()
  val currentYear         = Year.now.getValue
  val taxYearEndsThisYear = now.dayOfMonth().get() < 6 && now.monthOfYear().get() == 4 || now.monthOfYear().get() < 4

  val (taxYearStart, taxYearEnd) = if (taxYearEndsThisYear) (currentYear - 1, currentYear) else (currentYear, currentYear + 1)

  class SetUp extends MockAppConfig {
    val validator = new CreateValidator(mockAppConfig)
    MockedAppConfig.minTaxYearCisDeductions.returns("2019")
  }

  "running validation" should {
    "return no errors" when {
      "all the fields are submitted in a request" in new SetUp {

        validator.validate(CreateRawData(nino, requestJson)) shouldBe Nil
      }

      "an optional field is omitted in a request" in new SetUp {

        validator.validate(CreateRawData(nino, missingOptionalRequestJson)) shouldBe Nil
      }
    }
    "return errors" when {
      "invalid body type error" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, missingMandatoryFieldRequestJson)
        )
        result shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "an empty JSON period array is supplied as the request body" in new SetUp {
        private val result = validator.validate(CreateRawData(nino, missingPeriodDataRequestJson))
        result shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "invalid nino is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(invalidNino, requestJson)
        )
        result shouldBe List(NinoFormatError)
      }
      "invalid fromDate format is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidFromDateFormatRequestJson)
        )
        result shouldBe List(FromDateFormatError)
      }
      "invalid toDate format is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidToDateFormatRequestJson)
        )
        result shouldBe List(ToDateFormatError)
      }
      "invalid Deduction fromDate format is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidDeductionFromDateFormatRequestJson)
        )
        result shouldBe List(DeductionFromDateFormatError)
      }
      "invalid Deduction toDate format is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidDeductionToDateFormatRequestJson)
        )
        result shouldBe List(DeductionToDateFormatError)
      }
      "invalid deductionAmount too high is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidDeductionAmountTooHighRequestJson)
        )
        result shouldBe List(RuleDeductionAmountError)
      }
      "invalid deductionAmount negative is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidDeductionAmountNegativeRequestJson)
        )
        result shouldBe List(RuleDeductionAmountError)
      }
      "invalid CostOfMaterials too high is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidCostOfMaterialsTooHighRequestJson)
        )
        result shouldBe List(RuleCostOfMaterialsError)
      }
      "invalid CostOfMaterials negative is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidCostOfMaterialsNegativeRequestJson)
        )
        result shouldBe List(RuleCostOfMaterialsError)
      }
      "invalid GrossAmount too high is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidGrossAmountTooHighRequestJson)
        )
        result shouldBe List(RuleGrossAmountError)
      }
      "invalid GrossAmount negative is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidGrossAmountNegativeRequestJson)
        )
        result shouldBe List(RuleGrossAmountError)
      }
      "invalid toDate before fromDate is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidToDateBeforeFromDateRequestJson)
        )
        result shouldBe List(RuleDateRangeInvalidError)
      }
      "invalid date range above the maximum threshold is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, requestBodyJsonErrorInvalidDateRangeMax)
        )
        result shouldBe List(RuleDateRangeInvalidError)
      }
      "invalid date range below the threshold is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, requestBodyJsonErrorInvalidDateRangeMin)
        )
        result shouldBe List(RuleDateRangeInvalidError)
      }
      "invalid date range before minimum tax year is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, requestBodyJsonErrorNotSupportedTaxYear)
        )
        result shouldBe List(RuleTaxYearNotSupportedError)
      }
      "invalid employer reference format is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, requestBodyJsonErrorInvalidEmpRef)
        )
        result shouldBe List(EmployerRefFormatError)
      }
    }
  }

}
