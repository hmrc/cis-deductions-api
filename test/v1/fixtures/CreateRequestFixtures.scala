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

package v1.fixtures

import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.{JsValue, Json}
import v1.models.audit.{AuditError, AuditResponse}
import v1.models.request.amend.PeriodDetails
import v1.models.request.create.CreateBody
import v1.models.response.create.CreateResponseModel

object CreateRequestFixtures {

  val requestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
      |""".stripMargin
  }

  val missingOptionalRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
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
      |""".stripMargin
  }

  val missingPeriodDataRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |  ]
      |}
      |""".stripMargin
  }

  val invalidFromDateFormatRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "last week" ,
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
      |""".stripMargin
  }

  val invalidToDateFormatRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2020-04-06" ,
      |  "toDate": "yesterday",
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
      |""".stripMargin
  }

  val invalidDeductionFromDateFormatRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
      |""".stripMargin
  }

  val invalidDeductionToDateFormatRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
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
      |""".stripMargin
  }

  val invalidDeductionAmountTooHighRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
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
      |""".stripMargin
  }

  val invalidDeductionAmountNegativeRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
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
      |""".stripMargin
  }

  val invalidCostOfMaterialsTooHighRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
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
      |""".stripMargin
  }

  val invalidCostOfMaterialsNegativeRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2020-06-06",
      |      "deductionToDate": "2020-07-05",
      |      "costOfMaterials": -19.00,
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
      |""".stripMargin
  }

  val invalidGrossAmountTooHighRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
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
      |""".stripMargin
  }

  val invalidGrossAmountNegativeRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
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
      |""".stripMargin
  }

  val invalidToDateBeforeFromDateRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2021-04-06" ,
      |  "toDate": "2020-04-05",
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
      |""".stripMargin
  }

  val invalidFieldsRequestJson: JsValue = Json.parse {
    """
      |{
      |  "fromDate": "2020-04-06" ,
      |  "toDate": "2021-04-05",
      |  "contractorName": false,
      |  "employerRef": 78,
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
      |""".stripMargin
  }

  val requestBodyJsonErrorInvalidDateRangeMin = Json.parse(
    """
      |{
      |  "fromDate" : "2021-04-06",
      |  "toDate" : "2021-04-06",
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
        """.stripMargin
  )

  val requestBodyJsonErrorNotSupportedTaxYear = Json.parse(
    """
      |{
      |  "fromDate" : "2018-04-06",
      |  "toDate" : "2019-04-06",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
        """.stripMargin
  )

  val requestBodyJsonErrorInvalidEmpRef = Json.parse(
    """
      |{
      |  "fromDate" : "2020-04-06",
      |  "toDate" : "2021-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123AB56797",
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
        """.stripMargin
  )

  val requestObj: CreateBody = CreateBody(
    "2019-04-06",
    "2020-04-05",
    "Bovis",
    "123/AB56797",
    Seq(
      PeriodDetails(355.00, "2019-06-06", "2019-07-05", Some(35.00), Some(1457.00)),
      PeriodDetails(355.00, "2019-07-06", "2019-08-05", Some(35.00), Some(1457.00))
    )
  )

  val missingOptionalRequestObj: CreateBody = CreateBody(
    "2019-04-06",
    "2020-04-05",
    "Bovis",
    "123/AB56797",
    Seq(
      PeriodDetails(355.00, "2019-06-06", "2019-07-05", None, None),
      PeriodDetails(355.00, "2019-07-06", "2019-08-05", None, None)
    )
  )

  val responseJson: JsValue = Json.parse(
    """
      |{
      |"submissionId": "S4636A77V5KB8625U"
      |}
      |""".stripMargin
  )

  val invalidResponseJson: JsValue = Json.parse(
    """
      |{
      |"submissionId": 1
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

  val body: JsValue                = Json.parse("""{ "aField" : "aValue" }""")
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

  val hateoasResponse: (String, String) => String = (nino: String, responseId: String) => s"""
       |{
       |  "submissionId": "$responseId",
       |  "links":[
       |    {
       |      "href":"/individuals/deductions/cis/$nino/current-position",
       |      "rel":"retrieve-cis-deductions-for-subcontractor",
       |      "method":"GET"
       |     }
       |  ]
       |}
    """.stripMargin

  val emptyRequest = Json.parse(
    s"""
       |{
       |}
       |""".stripMargin
  )

  val deductionsResponseBody = Json.parse("""
      |{
      |   "submissionId":"someResponse",
      |   "links":[
      |      {
      |         "href":"/individuals/deductions/cis/AA123456A/current-position?fromDate=2019-04-06&toDate=2020-04-05",
      |         "method":"GET",
      |         "rel":"retrieve-cis-deductions-for-subcontractor"
      |      }
      |   ]
      |}
    """.stripMargin)

  def errorBody(code: String): String =
    s"""
       |      {
       |        "code": "$code",
       |        "reason": "des message"
       |      }
    """.stripMargin

  val requestBodyJson = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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

  val requestBodyJsonErrorFromDate = Json.parse(
    """
      |{
      |  "fromDate": "04-06-2020" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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

  val requestBodyJsonErrorToDate = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "04-05-2021",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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

  val requestBodyJsonErrorDeductionFromDate = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "06-06-2020",
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

  val requestBodyJsonErrorDeductionToDate = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-06-06",
      |      "deductionToDate": "07-05-2020",
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

  val requestBodyJsonErrorTaxYearNotSupported = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06" ,
      |  "toDate": "2019-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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

  val requestInvalidEmpRef = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123AB56797",
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

  val requestRuleDeductionAmountJson = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
      |      "deductionAmount": -355.00,
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

  val requestInvalidRuleCostOfMaterialsJson = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
      |      "costOfMaterials": -35.00,
      |      "grossAmountPaid": 1457.00
      |    }
      |  ]
      |}
        """.stripMargin
  )

  val requestInvalidGrossAmountJson = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
      |      "grossAmountPaid": -1457.00
      |    }
      |  ]
      |}
        """.stripMargin
  )

  val requestInvalidDateRangeJson = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06" ,
      |  "toDate": "2021-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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

  val requestBodyJsonFromDate13MonthsBeforeToDate = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2019-06-06",
      |      "deductionToDate": "2020-07-05",
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

}
