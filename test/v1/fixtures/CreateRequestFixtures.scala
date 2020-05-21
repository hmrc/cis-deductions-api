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

package v1.fixtures

import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.{JsValue, Json}
import v1.models.audit.{AuditError, AuditResponse}
import v1.models.request.{CreateRequest, PeriodDetails}
import v1.models.responseData.CreateResponseModel

object CreateRequestFixtures {

  val requestJson: JsValue = Json.parse {
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

  val requestBodyJsonErrorInvalidDateRangeMin = Json.parse(
    """
      |{
      |  "fromDate" : "2019-04-06",
      |  "toDate" : "2019-04-06",
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
        """.stripMargin
  )
  val requestBodyJsonErrorInvalidDateRangeMax = Json.parse(
    """
      |{
      |  "fromDate" : "2019-04-06",
      |  "toDate" : "2021-04-05",
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
        """.stripMargin
  )

  val requestObj: CreateRequest = CreateRequest("2019-04-06", "2020-04-05", "Bovis", "BV40092",
    Seq(
      PeriodDetails(355.00, "2019-06-06", "2019-07-05", Some(35.00), Some(1457.00)),
      PeriodDetails(355.00, "2019-07-06", "2019-08-05", Some(35.00), Some(1457.00))
    )
  )

  val missingOptionalRequestObj: CreateRequest = CreateRequest("2019-04-06", "2020-04-05", "Bovis", "BV40092",
    Seq(
      PeriodDetails(355.00, "2019-06-06", "2019-07-05", None, None),
      PeriodDetails(355.00, "2019-07-06", "2019-08-05", None, None)
    )
  )
  
  val   responseJson: JsValue = Json.parse(
    """
      |{
      |"id": "S4636A77V5KB8625U"
      |}
      |""".stripMargin
  )

  val invalidResponseJson: JsValue = Json.parse(
    """
      |{
      |"id": 1
      |}
      |""".stripMargin
  )

  val missingMandatoryResponseJson: JsValue = Json.parse(
    """
      |{
      |}
      |""".stripMargin
  )

  val responseObj: CreateResponseModel = CreateResponseModel("S4636A77V5KB8625U")


  val body: JsValue = Json.parse("""{ "aField" : "aValue" }""")
  val auditErrors: Seq[AuditError] = Seq(AuditError(errorCode = "FORMAT_NINO"))

  val auditResponseModelWithBody: AuditResponse =
    AuditResponse(
      httpStatus = OK,
      response = Right(Some(body))
    )

  val auditResponseJsonWithBody: JsValue = Json.parse(
    s"""
       |{
       |  "httpStatus": $OK,
       |  "body" : $body
       |}
    """.stripMargin
  )

  val auditResponseModelWithErrors: AuditResponse =
    AuditResponse(
      httpStatus = BAD_REQUEST,
      response = Left(auditErrors)
    )

  val auditResponseJsonWithErrors: JsValue = Json.parse(
    s"""
       |{
       |  "httpStatus": $BAD_REQUEST,
       |  "errors" : [
       |    {
       |      "errorCode" : "FORMAT_NINO"
       |    }
       |  ]
       |}
    """.stripMargin
  )

  val hateoasResponse: (String,String) => String = (nino: String, responseId: String) =>
    s"""
       |{
       |  "id": "$responseId",
       |  "links":[
       |    {
       |      "href":"/deductions/cis/$nino/current-position",
       |      "rel":"list-cis-deductions-for-subcontractor",
       |      "method":"GET"
       |     }
       |  ]
       |}
    """.stripMargin
}
