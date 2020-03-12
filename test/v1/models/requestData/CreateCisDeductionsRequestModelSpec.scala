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

package v1.models.requestData

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class CreateCisDeductionsRequestModelSpec extends UnitSpec {

  val cisDeductionsRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "BV40092",
      |  "periodData": [
      |      {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-06-06",
      |      "deductionToDate": "2019-07-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-07-06",
      |      "deductionToDate": "2019-08-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
      |""".stripMargin
  }

  val cisDeductionsRequestObj: CreateCisDeductionsRequestModel = CreateCisDeductionsRequestModel("2019-04-06", "2020-04-05", "Bovis", "BV40092",
    Seq(
      PeriodData(355.00, "2019-06-06", "2019-07-05", Some(35.00), 1457.00),
      PeriodData(355.00, "2019-07-06", "2019-08-05", Some(35.00), 1457.00)
    )
  )

  "CisDeductionsRequestModel" when {
    " read from valid JSON " should {
      "return the expected CisDeductionsRequestBody" in {
        Json.toJson(cisDeductionsRequestObj) shouldBe cisDeductionsRequestJson
      }
    }
  }
}
