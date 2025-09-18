/*
 * Copyright 2025 HM Revenue & Customs
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

package v2.fixtures

import play.api.libs.json.{JsValue, Json}
import v2.models.request.amend.{AmendBody, PeriodDetails}

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

  val requestJson: JsValue = Json.parse {
    """
      |{
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

  val requestJsonWithoutOptionalValues: JsValue = Json.parse {
    """
      |{
      |  "periodData": [
      |      {
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
      |""".stripMargin
  }

  val invalidFromDateForTaxYear: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-05" ,
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

  val invalidToDateForTaxYear: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-04",
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

  val invalidDateRange: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2021-04-05",
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

  val invalidRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": false,
      |  "periodData": [
      |      {
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
      |""".stripMargin
  }

  val missingOptionalRequestJson: JsValue = Json.parse {
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
      |      "deductionToDate": "2019-07-05"
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-07-06",
      |      "deductionToDate": "2019-08-05"
      |    }
      |  ]
      |}
      |""".stripMargin
  }

  val missingMandatoryFieldRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "BV40092",
      |  "periodData": [
      |      {
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

  val missingPeriodDataRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "BV40092",
      |  "periodData": [
      |  ]
      |}
      |""".stripMargin
  }

  val missingDeductionToDateRequestJson: JsValue = Json.parse {
    """
      |{
      |  "periodData": [
      |      {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-06-06",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-07-06",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
      |""".stripMargin

  }

  val emptyPeriodDataJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "last week" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "BV40092",
      |  "periodData": []
      |}
      |""".stripMargin

  }

  val invalidFromDateFormatRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "last week" ,
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

  val invalidToDateFormatRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "yesterday",
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

  val invalidDeductionFromDateFormatRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "BV40092",
      |  "periodData": [
      |      {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "yesterday",
      |      "deductionToDate": "2019-07-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "yesterday",
      |      "deductionToDate": "2019-08-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
      |""".stripMargin
  }

  val invalidRangeDeductionToDateFromDateFormatRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "BV40092",
      |  "periodData": [
      |      {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "1670-10-09",
      |      "deductionToDate": "2019-07-05",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2018-07-05",
      |      "deductionToDate": "2100-01-01",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
      |""".stripMargin
  }

  val invalidDeductionToDateFormatRequestJson: JsValue = Json.parse {
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
      |      "deductionToDate": "tomorrow",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    },
      |    {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-07-06",
      |      "deductionToDate": "tomorrow",
      |      "costOfMaterials": 35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
      |""".stripMargin
  }

  val invalidDeductionAmountTooHighRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "BV40092",
      |  "periodData": [
      |      {
      |      "deductionAmount": 99999999999999999999.00,
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

  val invalidDeductionAmountNegativeRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "BV40092",
      |  "periodData": [
      |      {
      |      "deductionAmount": -19.00,
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

  val invalidCostOfMaterialsTooHighRequestJson: JsValue = Json.parse {
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
      |      "costOfMaterials": 99999999999999999999.00,
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

  val invalidCostOfMaterialsNegativeRequestJson: JsValue = Json.parse {
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
      |      "costOfMaterials": -19.00,
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

  val invalidGrossAmountTooHighRequestJson: JsValue = Json.parse {
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
      |      "grossAmountPaid": 99999999999999999999.00
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

  val invalidGrossAmountNegativeRequestJson: JsValue = Json.parse {
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
      |      "grossAmountPaid": -19.00
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

  val invalidToDateBeforeFromDateRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2020-04-06" ,
      |  "toDate": "2019-04-05",
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

  val invalidFieldsRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": false,
      |  "employerRef": 78,
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

  val responseJson: JsValue = Json.parse(
    """
      |{
      |"id": "S4636A77V5KB8625U"
      |}
      |""".stripMargin
  )

  val hateoasResponse: (String, String) => String = (nino: String, responseId: String) => s"""
       |{
       |  "id": "$responseId",
       |  "links":[
       |    {
       |      "href":"/deductions/cis/$nino/current-position",
       |      "rel":"retrieve-cis-deductions-for-subcontractor",
       |      "method":"GET"
       |     }
       |  ]
       |}
    """.stripMargin

  val invalidDeductionDateRange: JsValue = Json.parse {
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

}
