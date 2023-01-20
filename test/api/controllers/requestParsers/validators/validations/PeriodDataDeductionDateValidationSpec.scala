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

import api.controllers.requestParsers.validators.validations.validations.NoValidationErrors
import api.models.errors.{DeductionFromDateFormatError, DeductionToDateFormatError, RuleDeductionsDateRangeInvalidError, RuleUnalignedDeductionsPeriodError}
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.request.amend.AmendRawData

class PeriodDataDeductionDateValidationSpec extends UnitSpec {

  val nino                   = "AA123456A"
  val submissionId           = "S4636A77V5KB8625U"
  val validFromDate          = "2020-04-06"
  val validToDate            = "2021-04-05"
  val validDeductionFromDate = "2020-07-06"
  val validDeductionToDate   = "2020-08-05"

  def requestDatesJson(deductionFromDate1: String = "2020-06-06",
                       deductionToDate1: String = "2020-07-05",
                       deductionFromDate2: String = "2020-07-06",
                       deductionToDate2: String = "2020-08-05"): JsValue = Json.parse(s"""
      |{
      |  "fromDate": "2020-04-06" ,
      |  "toDate": "2021-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "$deductionFromDate1",
      |      "deductionToDate": "$deductionToDate1",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "$deductionFromDate2",
      |      "deductionToDate": "$deductionToDate2",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
      |""".stripMargin)

  val validRawData: AmendRawData = AmendRawData(nino = nino, id = submissionId, body = requestDatesJson())

  val validRawDataDifferentCalendarYears: AmendRawData = AmendRawData(
    nino = nino,
    id = submissionId,
    body = requestDatesJson(
      deductionFromDate1 = "2018-12-01",
      deductionToDate1 = "2019-01-01",
      deductionFromDate2 = "2019-02-01",
      deductionToDate2 = "2019-03-01")
  )

  val invalidRawData: AmendRawData = AmendRawData(
    nino = nino,
    id = submissionId,
    body = requestDatesJson(deductionFromDate1 = "2018-06-06", deductionToDate1 = "2018-07-05")
  )

  val invalidRawDataSameCalendarYear: AmendRawData = AmendRawData(
    nino = nino,
    id = submissionId,
    body = requestDatesJson(
      deductionFromDate1 = "2019-01-01",
      deductionToDate1 = "2019-02-01",
      deductionFromDate2 = "2019-07-01",
      deductionToDate2 = "2019-08-01")
  )

  "running validateDate" should {
    "return no errors" when {
      "a json is submitted with a valid From date" in {
        PeriodDataDeductionDateValidation.validateDate(
          requestDatesJson(),
          "deductionFromDate",
          DeductionFromDateFormatError
        ) shouldBe NoValidationErrors
      }
      "a json is submitted with a valid To date" in {
        PeriodDataDeductionDateValidation.validateDate(
          requestDatesJson(),
          "deductionToDate",
          DeductionToDateFormatError
        ) shouldBe NoValidationErrors
      }
    }
    "return an error" when {
      "a json is submitted with an invalid from date" in {
        val invalidFromDate = "20420-06-06"

        PeriodDataDeductionDateValidation.validateDate(
          requestDatesJson(deductionFromDate1 = invalidFromDate),
          "deductionFromDate",
          DeductionFromDateFormatError
        ) shouldBe List(DeductionFromDateFormatError)
      }
      "a json is submitted with an invalid to date" in {
        val invalidToDate = "20250-07-05"

        PeriodDataDeductionDateValidation.validateDate(
          requestDatesJson(deductionToDate1 = invalidToDate),
          "deductionToDate",
          DeductionToDateFormatError
        ) shouldBe List(DeductionToDateFormatError)
      }
    }

    "running validateDateOrder" should {
      "return no errors" when {
        "the provided fromDate is 1 month before the provided toDate" in {
          PeriodDataDeductionDateValidation.validateDateOrder("2020-06-06", "2020-07-05") shouldBe NoValidationErrors
        }
        "the provided fromDate is in december and the toDate is in the following january" in {
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
        "the provided deductionFromDate and deductionToDate are inside the provided tax year" in {
          PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear(
            "2019-04-06",
            "2020-04-05",
            "2019-06-06",
            "2019-07-05") shouldBe NoValidationErrors
        }
        "the provided deductionFromDate and deductionToDate are the first month inside the provided tax year" in {
          PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear(
            "2019-04-06",
            "2020-04-05",
            "2019-04-06",
            "2019-05-05") shouldBe NoValidationErrors
        }
        "the provided deductionFromDate and deductionToDate are the last month inside the provided tax year" in {
          PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear(
            "2019-04-06",
            "2020-04-05",
            "2020-03-06",
            "2020-04-05") shouldBe NoValidationErrors
        }
        "the provided deductionFromDate and deductionToDate are December and January" in {
          PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear(
            "2019-04-06",
            "2020-04-05",
            "2019-12-06",
            "2020-01-05") shouldBe NoValidationErrors
        }
      }
      "return an error" when {
        "the provided deductionFromDate and deductionToDate are before the provided tax year" in {
          PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear("2019-04-06", "2020-04-05", "2019-01-06", "2019-02-05") shouldBe List(
            RuleUnalignedDeductionsPeriodError)
        }
        "the provided deductionFromDate and deductionToDate are after the provided tax year" in {
          PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear("2019-04-06", "2020-04-05", "2020-05-06", "2020-06-05") shouldBe List(
            RuleUnalignedDeductionsPeriodError)
        }
        "the provided deductionFromDate and deductionToDate are thirteen months apart with one inside the tax year" in {
          PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear("2019-04-06", "2020-04-05", "2019-06-06", "2020-07-05") shouldBe List(
            RuleUnalignedDeductionsPeriodError)
        }
      }
    }
    "running validateTaxYearForMultiplePeriods" should {
      "return no errors" when {
        "a json is submitted with multiple deductionToDates pointing to the same tax year" in {
          PeriodDataDeductionDateValidation.validateTaxYearForMultiplePeriods(validRawData) shouldBe NoValidationErrors
        }
        "a json is submitted with multiple deductionToDates pointing to the same tax year but are of different calendar years" in {
          PeriodDataDeductionDateValidation.validateTaxYearForMultiplePeriods(validRawDataDifferentCalendarYears) shouldBe NoValidationErrors
        }
      }
      "return an error" when {
        "a json is submitted with multiple deductionToDates pointing to different tax years" in {
          PeriodDataDeductionDateValidation.validateTaxYearForMultiplePeriods(invalidRawData) shouldBe List(RuleUnalignedDeductionsPeriodError)
        }
        "a json is submitted with multiple deductionToDates pointing to different tax years but are of the same calendar year" in {
          PeriodDataDeductionDateValidation.validateTaxYearForMultiplePeriods(invalidRawDataSameCalendarYear) shouldBe List(
            RuleUnalignedDeductionsPeriodError)
        }
      }
    }
  }

}
