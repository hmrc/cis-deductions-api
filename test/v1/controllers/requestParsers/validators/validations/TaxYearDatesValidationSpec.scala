/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import support.UnitSpec
import v1.models.errors.{RuleDateRangeInvalidError, RuleTaxYearNotEndedError}
import v1.models.utils.JsonErrorValidators

class TaxYearDatesValidationSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    "return no errors" when {
      "a request body with valid toDate & fromDate" in {
        val validationResult = TaxYearDatesValidation.validate("2019-04-06", "2020-04-05", 1, validateTaxYearEndedFlag = true)
        validationResult.isEmpty shouldBe true
      }
    }
    "return errors" when {
      "a request body with invalid toDate" in {
        val validationResult = TaxYearDatesValidation.validate("2019-04-06", "2020-04-06", 1, validateTaxYearEndedFlag = true)
        validationResult shouldBe List(RuleDateRangeInvalidError)
      }
      "a request body with invalid fromDate" in {
        val validationResult = TaxYearDatesValidation.validate("2019-04-07", "2020-04-05", 1, validateTaxYearEndedFlag = true)
        validationResult shouldBe List(RuleDateRangeInvalidError)
      }
      "a request body with invalid date range" in {
        val validationResult = TaxYearDatesValidation.validate("2018-04-06", "2020-04-05", 1, validateTaxYearEndedFlag = true)
        validationResult shouldBe List(RuleDateRangeInvalidError)
      }
    }
  }

  "validate" when {
    "validating the current year" should {
      "return a list containing an error" when {
        "a request using the current tax year is used" in {
          val currentYear = LocalDate.now().getYear
          val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
          val startYear = if (LocalDate.now().isAfter(LocalDate.parse(currentYear.toString + "-04-05", formatter))) currentYear else currentYear -1
          val endYear = startYear + 1
          val startDate = startYear.toString + "-04-06"
          val endDate = endYear.toString + "-04-05"
          val validationResult = TaxYearDatesValidation.validate(startDate, endDate, 1, validateTaxYearEndedFlag = true)
          validationResult shouldBe List(RuleTaxYearNotEndedError)
        }
        "a request using a future tax year is used" in {
          val currentYear = LocalDate.now().getYear
          val startYear = currentYear + 5
          val endYear = currentYear + 6
          val startDate = startYear.toString + "-04-06"
          val endDate = endYear.toString + "-04-05"
          val validationResult = TaxYearDatesValidation.validate(startDate, endDate, 1, validateTaxYearEndedFlag = true)
          validationResult shouldBe List(RuleDateRangeInvalidError)
        }
      }
    }
    "not validating the current year" should {
      "return an empty list" when {
        "a request using the current tax year is used" in {
          val currentYear = LocalDate.now().getYear
          val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
          val startYear = if (LocalDate.now().isAfter(LocalDate.parse(currentYear.toString + "-04-05", formatter))) currentYear else currentYear -1
          val endYear = startYear + 1
          val startDate = startYear.toString + "-04-06"
          val endDate = endYear.toString + "-04-05"
          val validationResult = TaxYearDatesValidation.validate(startDate, endDate, 1, validateTaxYearEndedFlag = false)
          validationResult shouldBe List()
        }
        "a request using a future tax year is used" in {
          val currentYear = LocalDate.now().getYear
          val startYear = currentYear + 5
          val endYear = currentYear + 6
          val startDate = startYear.toString + "-04-06"
          val endDate = endYear.toString + "-04-05"
          val validationResult = TaxYearDatesValidation.validate(startDate, endDate, 1, validateTaxYearEndedFlag = false)
          validationResult shouldBe List()
        }
      }
    }
  }
}
