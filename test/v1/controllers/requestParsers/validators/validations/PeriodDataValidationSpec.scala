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

package v1.controllers.requestParsers.validators.validations

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.errors.RuleIncorrectOrEmptyBodyError
import v1.models.request.amend.AmendRawData

class PeriodDataValidationSpec extends UnitSpec {

  val nino         = "AA123456A"
  val submissionId = "S4636A77V5KB8625U"

  val missingPeriodDataRequestJson: JsValue = Json.parse {
    """
      |{
      |  "periodData": [
      |  ]
      |}
      |""".stripMargin
  }

  val populatedPeriodDataRequestJson: JsValue = Json.parse("""
      |{
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

  val validRawData: AmendRawData   = AmendRawData(nino, submissionId, populatedPeriodDataRequestJson)
  val invalidRawData: AmendRawData = AmendRawData(nino, submissionId, missingPeriodDataRequestJson)

  "running emptyPeriodDataValidation" should {
    "return no errors" when {
      "the periodData array is populated" in {
        PeriodDataValidation.emptyPeriodDataValidation(validRawData) shouldBe NoValidationErrors
      }
    }
    "return an error" when {
      "the periodData array is empty" in {
        PeriodDataValidation.emptyPeriodDataValidation(invalidRawData) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
    }
  }

}
