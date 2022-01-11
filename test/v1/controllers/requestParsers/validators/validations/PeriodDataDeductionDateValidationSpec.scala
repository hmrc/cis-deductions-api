/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.errors._

class PeriodDataDeductionDateValidationSpec extends UnitSpec {

  val validFromDate = "2020-04-06"
  val validToDate = "2021-04-05"
  val validDeductionFromDate = "2020-07-06"
  val validDeductionToDate = "2020-08-05"
  val validDatesJson = Json.parse(
    """
      |{
      |  "fromDate": "2020-04-06" ,
      |  "toDate": "2021-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2020-06-06",
      |      "deductionToDate": "2020-07-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2020-07-06",
      |      "deductionToDate": "2020-08-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
      |""".stripMargin)
  val invalidDatesJson = Json.parse(
    """
      |{
      |  "fromDate": "2020-04-06" ,
      |  "toDate": "2021-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "20420-06-06",
      |      "deductionToDate": "20250-07-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2020-07-06",
      |      "deductionToDate": "2020-08-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
      |""".stripMargin)

  "running validateDate" should {
    "return no errors" when {
      "a json is submitted with a valid From date" in {
        PeriodDataDeductionDateValidation.validateDate(
          validDatesJson,
          "deductionFromDate",
          DeductionFromDateFormatError
        ) shouldBe NoValidationErrors
      }
      "a json is submitted with a valid To date" in {
        PeriodDataDeductionDateValidation.validateDate(
          validDatesJson,
          "deductionToDate",
          DeductionToDateFormatError
        ) shouldBe NoValidationErrors
      }
    }
    "return an error" when {
      "a json is submitted with an invalid From date" in {
        PeriodDataDeductionDateValidation.validateDate(
          invalidDatesJson,
          "deductionFromDate",
          DeductionFromDateFormatError
        ) shouldBe List(DeductionFromDateFormatError)
      }
      "a json is submitted with an invalid To date" in {
        PeriodDataDeductionDateValidation.validateDate(
          invalidDatesJson,
          "deductionToDate",
          DeductionToDateFormatError
        ) shouldBe List(DeductionToDateFormatError)
      }
    }
  }

  "running validateDateOrder" should {
    "return no errors" when {
      "the provided fromDate is 1 month before the provided toDate" in {
        PeriodDataDeductionDateValidation.validateDateOrder("2020-06-06", "2020-07-05") shouldBe NoValidationErrors
      }
      "the provided fromDate is in december and the to date is in the following january" in {
        PeriodDataDeductionDateValidation.validateDateOrder("2019-12-06", "2020-01-05") shouldBe NoValidationErrors
      }
    }
    "return an error" when {
      "the provided fromDate is after the provided toDate" in {
        PeriodDataDeductionDateValidation.validateDateOrder("2020-05-06", "2020-07-05") shouldBe List(RuleDeductionsDateRangeInvalidError)
      }
      "the provided fromDate is before the provided toDate" in {
        PeriodDataDeductionDateValidation.validateDateOrder("2020-07-05", "2020-07-05") shouldBe List(RuleDeductionsDateRangeInvalidError)
      }
    }
  }

  "running validatePeriodInsideTaxYear" should {
    "return no errors" when {
      "the provided deductionFromDate and deductionFromDate are inside the provided tax year" in {
        PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear("2019-04-06", "2020-04-05", "2019-06-06", "2019-07-05") shouldBe NoValidationErrors
      }
      "the provided deductionFromDate and deductionFromDate are first month inside the provided tax year" in {
        PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear("2019-04-06", "2020-04-05", "2019-04-06", "2019-05-05") shouldBe NoValidationErrors
      }
      "the provided deductionFromDate and deductionFromDate are last month inside the provided tax year" in {
        PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear("2019-04-06", "2020-04-05", "2020-03-06", "2020-04-05") shouldBe NoValidationErrors
      }
      "the provided deductionFromDate and deductionFromDate are december and jan" in {
        PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear("2019-04-06", "2020-04-05", "2019-12-06", "2020-01-05") shouldBe NoValidationErrors
      }
    }
    "return an error" when {
      "the provided deductionFromDate and deductionFromDate are before the provided tax year" in {
        PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear("2019-04-06", "2020-04-05", "2019-01-06", "2019-02-05") shouldBe List(RuleUnalignedDeductionsPeriodError)
      }
      "the provided deductionFromDate and deductionFromDate are after the provided tax year" in {
        PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear("2019-04-06", "2020-04-05", "2020-05-06", "2020-06-05") shouldBe List(RuleUnalignedDeductionsPeriodError)
      }
      "the provided deductionFromDate and deductionFromDate are thirteen months apart with one inside the tax year" in {
        PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear("2019-04-06", "2020-04-05", "2019-06-06", "2020-07-05") shouldBe List(RuleUnalignedDeductionsPeriodError)
      }
    }
  }

}
