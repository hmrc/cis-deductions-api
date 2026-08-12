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

import api.models.domain.TaxYear.currentTaxYear
import play.api.libs.json.{JsValue, Json}
import v3.models.request.amend.PeriodDetails
import v3.models.request.create.CreateBody
import v3.models.response.create.CreateResponseModel

import java.time.LocalDate

object CreateRequestFixtures {

  val parsedRequestData: CreateBody = CreateBody(
    fromDate = "2019-04-06",
    toDate = "2020-04-05",
    contractorName = "Bovis",
    employerRef = "123/AB56797",
    periodData = Seq(
      PeriodDetails(355.00, "2019-06-06", "2019-07-05", Some(35.00), Some(1457.00)),
      PeriodDetails(355.00, "2019-07-06", "2019-08-05", Some(35.00), Some(1457.00))
    )
  )

  val parsedRequestDataMissingOptional: CreateBody = CreateBody(
    fromDate = "2019-04-06",
    toDate = "2020-04-05",
    contractorName = "Bovis",
    employerRef = "123/AB56797",
    periodData = Seq(
      PeriodDetails(355.00, "2019-06-06", "2019-07-05", None, None),
      PeriodDetails(355.00, "2019-07-06", "2019-08-05", None, None)
    )
  )

  def requestJsonWithDates(fromDate: String, toDate: String): JsValue = Json.parse(
    s"""
      |{
      |  "fromDate" : "$fromDate",
      |  "toDate" : "$toDate",
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

  val requestJson: JsValue = requestJsonWithDates(fromDate = "2019-04-06", toDate = "2020-04-05")

  val requestBodyJsonTys: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2023-04-06",
      |  "toDate": "2024-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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

  val requestJsonCurrentTaxYear: JsValue = {
    val currentTaxYearStartDate: LocalDate = currentTaxYear.startDate
    val currentTaxYearEndDate: LocalDate   = currentTaxYear.endDate

    Json.parse(
      s"""
        |{
        |  "fromDate": "$currentTaxYearStartDate",
        |  "toDate": "$currentTaxYearEndDate",
        |  "contractorName": "Bovis",
        |  "employerRef": "123/AB56797",
        |  "periodData": [
        |    {
        |      "deductionAmount": 355.00,
        |      "deductionFromDate": "${currentTaxYearStartDate.getYear}-06-06",
        |      "deductionToDate": "${currentTaxYearStartDate.getYear}-07-05",
        |      "costOfMaterials": 35.00,
        |      "grossAmountPaid": 1457.00
        |    },
        |    {
        |      "deductionAmount": 355.00,
        |      "deductionFromDate": "${currentTaxYearStartDate.getYear}-07-06",
        |      "deductionToDate": "${currentTaxYearStartDate.getYear}-08-05",
        |      "costOfMaterials": 35.00,
        |      "grossAmountPaid": 1457.00
        |    }
        |  ]
        |}
      """.stripMargin
    )
  }

  val invalidRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": false,
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
    """.stripMargin
  )

  val missingOptionalRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |    {
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

  val emptyPeriodDataJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": []
      |}
    """.stripMargin
  )

  val requestJsonErrorFromDate: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "04-06-2020",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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

  val requestJsonErrorToDate: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06",
      |  "toDate": "04-05-2021",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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

  val requestJsonErrorToDateBeforeFromDate: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2021-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
    """.stripMargin
  )

  val requestJsonErrorTaxYearNotSupported: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06",
      |  "toDate": "2019-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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

  val requestJsonErrorDateRangeMax: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06",
      |  "toDate": "2021-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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

  val requestJsonErrorEmpRef: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123AB56797",
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

  val requestJsonErrorDeductionAmountTooHigh: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
    """.stripMargin
  )

  val requestJsonErrorDeductionToDate: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
      |  "fromDate": "2197-04-06",
      |  "toDate": "2198-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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

  val requestJsonErrorDeductionPeriodsOutsideTaxYear: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
    """.stripMargin
  )

  val requestJsonErrorDeductionPeriodNotAligned: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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
      |  "fromDate": "2019-04-06",
      |  "toDate": "2020-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
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

  val responseJson: JsValue = Json.parse(
    """
      |{
      |  "submissionId": "S4636A77V5KB8625U"
      |}
    """.stripMargin
  )

  val invalidResponseJson: JsValue = Json.parse(
    """
      |{
      |  "submissionId": 1
      |}
    """.stripMargin
  )

  val missingMandatoryResponseJson: JsValue = Json.parse(
    """
      |{}
    """.stripMargin
  )

  val responseObj: CreateResponseModel = CreateResponseModel("S4636A77V5KB8625U")

  val createDeductionResponseBody: JsValue = Json.parse(
    """
      |{
      |  "submissionId": "someResponse"
      |}
    """.stripMargin
  )

  def errorBody(code: String): String =
    s"""
      |{
      |  "failures": [
      |    {
      |      "code": "$code",
      |      "reason": "downstream message"
      |    }
      |  ]
      |}
    """.stripMargin

}
