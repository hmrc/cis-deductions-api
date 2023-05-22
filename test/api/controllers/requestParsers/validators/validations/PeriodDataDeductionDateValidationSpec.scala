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

import api.models.errors.{DeductionFromDateFormatError, DeductionToDateFormatError}
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

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

  }

}
