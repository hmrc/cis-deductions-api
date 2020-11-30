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
      |      "deductionFromDate": "202520-06-06",
      |      "deductionToDate": "20253740-07-05",
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

  "running validateDate " should {
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
        ) shouldBe DeductionFromDateFormatError
      }
      "a json is submitted with an invalid To date" in {
        PeriodDataDeductionDateValidation.validateDate(
          invalidDatesJson,
          "deductionToDate",
          DeductionToDateFormatError
        ) shouldBe DeductionToDateFormatError
      }
    }

  }


}
