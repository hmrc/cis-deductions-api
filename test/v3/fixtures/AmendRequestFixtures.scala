/*
 * Copyright 2026 HM Revenue & Customs
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

package v3.fixtures

import play.api.libs.json.{JsValue, Json}
import v3.models.request.amend.{AmendBody, PeriodDetails}

object AmendRequestFixtures {

  val amendRequestObj: AmendBody = AmendBody(
    List(
      PeriodDetails(355.00, "2019-06-06", "2019-07-05", Some(35.00), Some(1457.00)),
      PeriodDetails(355.00, "2019-07-06", "2019-08-05", Some(35.00), Some(1457.00))
    )
  )

  val amendMissingOptionalRequestObj: AmendBody = AmendBody(
    List(
      PeriodDetails(355.00, "2019-06-06", "2019-07-05", None, None),
      PeriodDetails(355.00, "2019-07-06", "2019-08-05", None, None)
    )
  )

  val requestJson: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
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
    """.stripMargin
  )

  val requestBodyJsonTys: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2023-06-06",
      |      "deductionToDate": "2023-07-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2023-07-06",
      |      "deductionToDate": "2023-08-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val requestJsonWithoutOptionalValues: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-06-06",
      |      "deductionToDate": "2019-07-05"
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-07-06",
      |      "deductionToDate": "2019-08-05"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val invalidRequestJson: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-06-06",
      |      "deductionToDate": "2019-07-05",
      |      "costOfMaterials": false,
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
    """.stripMargin
  )

  val missingOptionalRequestJson: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-06-06",
      |      "deductionToDate": "2019-07-05"
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-07-06",
      |      "deductionToDate": "2019-08-05"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val missingMandatoryFieldRequestJson: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
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
    """.stripMargin
  )

  val emptyPeriodDataJson: JsValue = Json.parse(
    """
      |{
      |  "periodData": []
      |}
    """.stripMargin
  )

  val requestJsonErrorTaxYearNotSupported: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2018-06-06",
      |      "deductionToDate": "2018-07-05",
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
    """.stripMargin
  )

  val requestJsonErrorDeductionAmountTooHigh: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": 99999999999999999999.00,
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
    """.stripMargin
  )

  val requestJsonErrorDeductionAmountNegative: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": -19.00,
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
    """.stripMargin
  )

  val requestJsonErrorDeductionFromDate: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |      {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "yesterday",
      |      "deductionToDate": "2020-07-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "yesterday",
      |      "deductionToDate": "2020-08-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val requestJsonErrorDeductionToDate: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2020-06-06",
      |      "deductionToDate": "tomorrow",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2020-07-06",
      |      "deductionToDate": "tomorrow",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val requestJsonErrorDeductionToDateBeforeFromDate: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-07-06",
      |      "deductionToDate": "2019-06-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-08-06",
      |      "deductionToDate": "2019-07-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val requestJsonErrorDatesOutsideSupportedRange: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "1899-12-31",
      |      "deductionToDate": "1993-09-17",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2020-07-06",
      |      "deductionToDate": "2200-09-02",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val requestJsonErrorDeductionPeriodNotAligned: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-06-07",
      |      "deductionToDate": "2019-07-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-07-06",
      |      "deductionToDate": "2019-08-06",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val requestJsonErrorDuplicateDeductionPeriods: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-06-06",
      |      "deductionToDate": "2019-07-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-06-06",
      |      "deductionToDate": "2019-07-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val requestJsonErrorCostOfMaterialsTooHigh: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2020-06-06",
      |      "deductionToDate": "2020-07-05",
      |      "costOfMaterials": 99999999999999999999.00,
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
    """.stripMargin
  )

  val requestJsonErrorCostOfMaterialsNegative: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2020-06-06",
      |      "deductionToDate": "2020-07-05",
      |      "costOfMaterials": 19.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2020-07-06",
      |      "deductionToDate": "2020-08-05",
      |      "costOfMaterials": -35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val requestJsonErrorGrossAmountPaidTooHigh: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2020-06-06",
      |      "deductionToDate": "2020-07-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 99999999999999999999.00
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
    """.stripMargin
  )

  val requestJsonErrorGrossAmountPaidNegative: JsValue = Json.parse(
    """
      |{
      |  "periodData": [
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2020-06-06",
      |      "deductionToDate": "2020-07-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": -19.00
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
    """.stripMargin
  )

}
