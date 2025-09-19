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

object RetrieveJson {

  val multipleDeductionsJson: JsValue = Json.parse(
    """
      |{
      | "totalDeductionAmount": 12345.56,
      | "totalCostOfMaterials": 234234.33,
      | "totalGrossAmountPaid": 2342424.56,
      | "cisDeductions" : [
      |  {
      |    "fromDate": "2020-04-06" ,
      |    "toDate": "2021-04-05",
      |    "contractorName": "Bovis",
      |    "employerRef": "BV40092",
      |    "totalDeductionAmount": 3543.55,
      |    "totalCostOfMaterials": 6644.67,
      |    "totalGrossAmountPaid": 3424.12,
      |    "periodData": [
      |      {
      |        "deductionFromDate": "2020-04-06",
      |        "deductionToDate": "2020-05-05",
      |        "deductionAmount": 355.11,
      |        "costOfMaterials": 35.11,
      |        "grossAmountPaid": 1457.11,
      |        "submissionDate": "2020-05-11T16:38:57.489Z",
      |        "source": "contractor"
      |      },
      |      {
      |        "deductionFromDate": "2020-05-06",
      |        "deductionToDate": "2020-06-05",
      |        "deductionAmount": 355.11,
      |        "costOfMaterials": 35.11,
      |        "grossAmountPaid": 1457.11,
      |        "submissionDate": "2020-06-11T16:38:57.489Z",
      |        "source": "contractor"
      |      },
      |      {
      |        "deductionFromDate": "2020-06-06",
      |        "deductionToDate": "2020-07-05",
      |        "deductionAmount": 355.11,
      |        "costOfMaterials": 35.11,
      |        "grossAmountPaid": 1457.11,
      |        "submissionDate": "2020-07-11T16:38:57.489Z",
      |        "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
      |        "source": "customer"
      |      },
      |      {
      |        "deductionFromDate": "2020-07-06",
      |        "deductionToDate": "2020-08-05",
      |        "deductionAmount": 355.11,
      |        "costOfMaterials": 35.11,
      |        "grossAmountPaid": 1457.11,
      |        "submissionDate": "2020-08-11T16:38:57.489Z",
      |        "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
      |        "source": "customer"
      |      }
      |    ]
      |  },
      |  {
      |    "fromDate": "2020-04-06",
      |    "toDate": "2021-04-05",
      |    "contractorName": "Taylor Wimpy",
      |    "employerRef": "TW44355",
      |    "totalDeductionAmount": 3543.55,
      |    "totalCostOfMaterials": 6644.67,
      |    "totalGrossAmountPaid": 3424.12,
      |    "periodData": [
      |      {
      |        "deductionFromDate": "2020-07-06",
      |        "deductionToDate": "2020-08-05",
      |        "deductionAmount": 60.11,
      |        "costOfMaterials": 35.11,
      |        "grossAmountPaid": 1457.11,
      |        "submissionDate": "2020-08-11T16:38:57.489Z",
      |        "source": "contractor"
      |      },
      |      {
      |        "deductionFromDate": "2020-09-06",
      |        "deductionToDate": "2020-10-05",
      |        "deductionAmount": 60.11,
      |        "costOfMaterials": 35.11,
      |        "grossAmountPaid": 1457.11,
      |        "submissionDate": "2020-10-11T16:38:57.489Z",
      |        "source": "contractor"
      |      }
      |    ]
      |  }
      |]
      |}
      |""".stripMargin
  )

  def singleDeductionJson(fromDate: String = "2019-04-06", toDate: String = "2020-04-05"): JsValue = Json.parse {
    s"""
       |{
       | "totalDeductionAmount": 12345.56,
       | "totalCostOfMaterials": 234234.33,
       | "totalGrossAmountPaid": 2342424.56,
       | "cisDeductions" : [
       |  {
       |    "fromDate": "$fromDate",
       |    "toDate": "$toDate",
       |    "contractorName": "contractor Name",
       |    "employerRef": "123/AA12345",
       |    "totalDeductionAmount": 3543.55,
       |    "totalCostOfMaterials": 6644.67,
       |    "totalGrossAmountPaid": 3424.12,
       |    "periodData": [
       |      {
       |        "deductionAmount": 355.00,
       |        "deductionFromDate": "2019-06-06",
       |        "deductionToDate": "2019-07-05",
       |        "costOfMaterials": 35.00,
       |        "grossAmountPaid": 1457.00,
       |        "submissionDate": "2020-07-11T16:38:57.489Z",
       |        "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
       |        "source": "customer"
       |      },
       |      {
       |        "deductionAmount": 355.00,
       |        "deductionFromDate": "2019-07-06",
       |        "deductionToDate": "2019-08-05",
       |        "costOfMaterials": 35.00,
       |        "grossAmountPaid": 1457.00,
       |        "submissionDate": "2020-08-11T16:38:57.489Z",
       |        "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
       |        "source": "customer"
       |      }
       |    ]
       |  }
       |]
       |}
       |""".stripMargin
  }

  val singleDeductionWithoutIdsJson: JsValue = Json.parse {
    """
      |{
      | "totalDeductionAmount": 12345.56,
      | "totalCostOfMaterials": 234234.33,
      | "totalGrossAmountPaid": 2342424.56,
      | "cisDeductions" : [
      |  {
      |    "fromDate": "2019-04-06" ,
      |    "toDate": "2020-04-05",
      |    "contractorName": "contractor Name",
      |    "employerRef": "123/AA12345",
      |    "totalDeductionAmount": 3543.55,
      |    "totalCostOfMaterials": 6644.67,
      |    "totalGrossAmountPaid": 3424.12,
      |    "periodData": [
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2019-06-06",
      |        "deductionToDate": "2019-07-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-07-11T16:38:57.489Z",
      |        "source": "customer"
      |      },
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2019-07-06",
      |        "deductionToDate": "2019-08-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-08-11T16:38:57.489Z",
      |        "source": "customer"
      |      }
      |    ]
      |  }
      |]
      |}
      |""".stripMargin
  }

  val singleDeductionContractorJson: JsValue = Json.parse {
    """
      |{
      | "totalDeductionAmount": 12345.56,
      | "totalCostOfMaterials": 234234.33,
      | "totalGrossAmountPaid": 2342424.56,
      | "cisDeductions" : [
      |  {
      |    "fromDate": "2019-04-06" ,
      |    "toDate": "2020-04-05",
      |    "contractorName": "contractor Name",
      |    "employerRef": "123/AA12345",
      |    "totalDeductionAmount": 3543.55,
      |    "totalCostOfMaterials": 6644.67,
      |    "totalGrossAmountPaid": 3424.12,
      |    "periodData": [
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2019-06-06",
      |        "deductionToDate": "2019-07-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-07-11T16:38:57.489Z",
      |        "source": "contractor"
      |      },
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2019-07-06",
      |        "deductionToDate": "2019-08-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-08-11T16:38:57.489Z",
      |        "source": "contractor"
      |      }
      |    ]
      |  }
      |]
      |}
      |""".stripMargin
  }

  def singleDeductionJsonHateoas(fromDate: String, toDate: String, taxYearParam: String, isTys: Boolean = false): JsValue = Json.parse {
    s"""
       |{
       |"totalDeductionAmount": 12345.56,
       |"totalCostOfMaterials": 234234.33,
       |"totalGrossAmountPaid": 2342424.56,
       | "cisDeductions" : [
       |  {
       |    "fromDate": "$fromDate",
       |    "toDate": "$toDate",
       |    "contractorName": "contractor Name",
       |    "employerRef": "123/AA12345",
       |    "totalDeductionAmount": 3543.55,
       |    "totalCostOfMaterials": 6644.67,
       |    "totalGrossAmountPaid": 3424.12,
       |    "periodData": [
       |      {
       |        "deductionFromDate": "2019-06-06",
       |        "deductionToDate": "2019-07-05",
       |        "deductionAmount": 355.00,
       |        "costOfMaterials": 35.00,
       |        "grossAmountPaid": 1457.00,
       |        "submissionDate": "2020-07-11T16:38:57.489Z",
       |        "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
       |        "source": "customer"
       |      },
       |      {
       |        "deductionFromDate": "2019-07-06",
       |        "deductionToDate": "2019-08-05",
       |        "deductionAmount": 355.00,
       |        "costOfMaterials": 35.00,
       |        "grossAmountPaid": 1457.00,
       |        "submissionDate": "2020-08-11T16:38:57.489Z",
       |        "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
       |        "source": "customer"
       |      }
       |    ],
       |    "links": [
       |                {
       |                    "href": "/individuals/deductions/cis/AA123456A/amendments/4557ecb5-fd32-48cc-81f5-e6acd1099f3c${
        if (isTys)
          s"?taxYear=$taxYearParam"
        else ""
      }",
       |                    "method": "DELETE",
       |                    "rel": "delete-cis-deductions-for-subcontractor"
       |                },
       |                {
       |                    "href": "/individuals/deductions/cis/AA123456A/amendments/4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
       |                    "method": "PUT",
       |                    "rel": "amend-cis-deductions-for-subcontractor"
       |                }
       |            ]
       |  }
       |],
       |    "links": [
       |        {
       |            "href": "/individuals/deductions/cis/AA123456A/current-position/$taxYearParam/customer",
       |            "method": "GET",
       |            "rel": "self"
       |        },
       |        {
       |            "href": "/individuals/deductions/cis/AA123456A/amendments",
       |            "method": "POST",
       |            "rel": "create-cis-deductions-for-subcontractor"
       |        }
       |    ]
       |}
       |""".stripMargin
  }

  val singleDeductionWithoutIdsJsonHateoas: JsValue = Json.parse {
    """
      |{
      |"totalDeductionAmount": 12345.56,
      |"totalCostOfMaterials": 234234.33,
      |"totalGrossAmountPaid": 2342424.56,
      | "cisDeductions" : [
      |  {
      |    "fromDate": "2019-04-06",
      |    "toDate": "2020-04-05",
      |    "contractorName": "contractor Name",
      |    "employerRef": "123/AA12345",
      |    "totalDeductionAmount": 3543.55,
      |    "totalCostOfMaterials": 6644.67,
      |    "totalGrossAmountPaid": 3424.12,
      |    "periodData": [
      |      {
      |        "deductionFromDate": "2019-06-06",
      |        "deductionToDate": "2019-07-05",
      |        "deductionAmount": 355.00,
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-07-11T16:38:57.489Z",
      |        "source": "customer"
      |      },
      |      {
      |        "deductionFromDate": "2019-07-06",
      |        "deductionToDate": "2019-08-05",
      |        "deductionAmount": 355.00,
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-08-11T16:38:57.489Z",
      |        "source": "customer"
      |      }
      |    ]
      |  }
      |],
      |    "links": [
      |        {
      |            "href": "/individuals/deductions/cis/AA123456A/current-position/2019-20/customer",
      |            "method": "GET",
      |            "rel": "self"
      |        },
      |        {
      |            "href": "/individuals/deductions/cis/AA123456A/amendments",
      |            "method": "POST",
      |            "rel": "create-cis-deductions-for-subcontractor"
      |        }
      |    ]
      |}
      |""".stripMargin
  }

  val singleDeductionJsonHateoasMissingOptionalField: JsValue = Json.parse {
    """
      |{
      |"totalDeductionAmount": 12345.56,
      |"totalCostOfMaterials": 234234.33,
      |"totalGrossAmountPaid": 2342424.56,
      | "cisDeductions" : [
      |  {
      |     "fromDate": "2019-04-06",
      |     "toDate": "2020-04-05",
      |     "contractorName": "contractor Name",
      |     "employerRef": "123/AA12345",
      |     "totalDeductionAmount": 3543.55,
      |     "totalCostOfMaterials": 6644.67,
      |     "totalGrossAmountPaid": 3424.12,
      |     "periodData": [
      |      {
      |        "deductionFromDate": "2019-06-06",
      |        "deductionToDate": "2019-07-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-07-11T16:38:57.489Z",
      |        "source": "contractor"
      |      },
      |      {
      |        "deductionFromDate": "2019-07-06",
      |        "deductionToDate": "2019-08-05",
      |        "deductionAmount": 355.00,
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-08-11T16:38:57.489Z",
      |        "source": "contractor"
      |      }
      |    ]
      |  }
      |],
      |    "links": [
      |        {
      |            "href": "/individuals/deductions/cis/AA123456A/current-position/2019-20/customer",
      |            "method": "GET",
      |            "rel": "self"
      |        },
      |        {
      |            "href": "/individuals/deductions/cis/AA123456A/amendments",
      |            "method": "POST",
      |            "rel": "create-cis-deductions-for-subcontractor"
      |        }
      |    ]
      |}
      |""".stripMargin
  }

  val singleDeductionJsonHateoasNoId: JsValue = Json.parse {
    """
      |{
      |"totalDeductionAmount": 12345.56,
      |"totalCostOfMaterials": 234234.33,
      |"totalGrossAmountPaid": 2342424.56,
      | "cisDeductions" : [
      |  {
      |    "fromDate": "2019-04-06",
      |    "toDate": "2020-04-05",
      |    "contractorName": "contractor Name",
      |    "employerRef": "123/AA12345",
      |    "totalDeductionAmount": 3543.55,
      |    "totalCostOfMaterials": 6644.67,
      |    "totalGrossAmountPaid": 3424.12,
      |    "periodData": [
      |      {
      |        "deductionFromDate": "2019-06-06",
      |        "deductionToDate": "2019-07-05",
      |        "deductionAmount": 355.00,
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-07-11T16:38:57.489Z",
      |        "source": "contractor"
      |      },
      |      {
      |        "deductionFromDate": "2019-07-06",
      |        "deductionToDate": "2019-08-05",
      |        "deductionAmount": 355.00,
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-08-11T16:38:57.489Z",
      |        "source": "contractor"
      |      }
      |     ]
      |  }
      |],
      |    "links": [
      |        {
      |            "href": "/individuals/deductions/cis/AA123456A/current-position/2019-20/customer",
      |            "method": "GET",
      |            "rel": "self"
      |        },
      |        {
      |            "href": "/individuals/deductions/cis/AA123456A/amendments",
      |            "method": "POST",
      |            "rel": "create-cis-deductions-for-subcontractor"
      |        }
      |    ]
      |}
      |""".stripMargin
  }

  val singleDeductionRequestJson: String =
    """
      |
      |""".stripMargin

  val errorJson: String =
    """
      |{
      | "totalDeductionAmount": "deduction amount: 12345.56",
      | "totalCostOfMaterials": 234234.33,
      | "totalGrossAmountPaid": 2342424.56,
      | "cisDeductions" : [
      |  {
      |    "submissionId": "54759eb3c090d83494e2d804",
      |    "fromDate": "2020-04-06" ,
      |    "toDate": "2021-04-05",
      |    "contractorName": "contractor Name",
      |    "employerRef": "123/AA12345",
      |    "totalDeductionAmount": 3543.55,
      |    "totalCostOfMaterials": 6644.67,
      |    "totalGrossAmountPaid": 3424.12,
      |    "periodData": [
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2020-06-06",
      |        "deductionToDate": "2020-07-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-07-11T16:38:57.489Z",
      |        "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
      |        "source": "customer"
      |      },
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2020-07-06",
      |        "deductionToDate": "2020-08-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-08-11T16:38:57.489Z",
      |        "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
      |        "source": "customer"
      |      }
      |    ]
      |  }
      |]
      |}
      |""".stripMargin

}
