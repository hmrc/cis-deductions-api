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

import api.models.errors.RuleIncorrectOrEmptyBodyError
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class PeriodDataValidationSpec extends UnitSpec {

  val missingPeriodDataRequestJson: JsValue = Json.parse {
    """
      |{
      |  "periodData": []
      |}
      |""".stripMargin
  }

  val populatedPeriodDataRequestJson: JsValue = Json.parse("""
      |{
      |  "periodData": [
      |    {
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

  "running emptyPeriodDataValidation" should {
    "return no errors" when {
      "the periodData array is populated" in {
        PeriodDataValidation.emptyPeriodDataValidation(populatedPeriodDataRequestJson) shouldBe NoValidationErrors
      }
    }

    "return an error" when {
      "the periodData array is empty" in {
        PeriodDataValidation.emptyPeriodDataValidation(missingPeriodDataRequestJson) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
    }
  }

}
