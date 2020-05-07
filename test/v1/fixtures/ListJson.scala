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

import play.api.libs.json.{JsValue, Json}


object ListJson {

  val multipleDeductionsJson: JsValue = Json.parse(
    """
      |{
      | "cisDeductions" : [
      |  {
      |    "submissionId": "54759eb3c090d83494e2d804",
      |    "fromDate": "2019-04-06" ,
      |    "toDate": "2020-04-05",
      |    "contractorName": "Bovis",
      |    "employerRef": "BV40092",
      |    "periodData": [
      |      {
      |        "deductionAmount": 355.11,
      |        "deductionFromDate": "2019-04-06",
      |        "deductionToDate": "2019-05-05",
      |        "costOfMaterials": 35.11,
      |        "grossAmountPaid": 1457.11,
      |        "submissionDate": "2019-04-03",
      |        "submittedBy": "contractor"
      |      },
      |      {
      |        "deductionAmount": 355.11,
      |        "deductionFromDate": "2019-05-06",
      |        "deductionToDate": "2019-06-05",
      |        "costOfMaterials": 35.11,
      |        "grossAmountPaid": 1457.11,
      |        "submissionDate": "2019-05-03",
      |        "submittedBy": "contractor"
      |      },
      |      {
      |        "deductionAmount": 355.11,
      |        "deductionFromDate": "2019-06-06",
      |        "deductionToDate": "2019-07-05",
      |        "costOfMaterials": 35.11,
      |        "grossAmountPaid": 1457.11,
      |        "submissionDate": "2020-01-14",
      |        "submittedBy": "customer"
      |      },
      |      {
      |        "deductionAmount": 355.11,
      |        "deductionFromDate": "2019-07-06",
      |        "deductionToDate": "2019-08-05",
      |        "costOfMaterials": 35.11,
      |        "grossAmountPaid": 1457.11,
      |        "submissionDate": "2020-01-14",
      |        "submittedBy": "customer"
      |      }
      |    ]
      |  },
      |  {
      |    "fromDate": "2019-04-06",
      |    "toDate": "2020-04-05",
      |    "contractorName": "Taylor Wimpy",
      |    "employerRef": "TW44355",
      |    "periodData": [
      |      {
      |        "deductionAmount": 60.11,
      |        "deductionFromDate": "2019-07-06",
      |        "deductionToDate": "2019-08-05",
      |        "costOfMaterials": 35.11,
      |        "grossAmountPaid": 1457.11,
      |        "submissionDate": "2019-08-05",
      |        "submittedBy": "contractor"
      |      },
      |      {
      |        "deductionAmount": 60.11,
      |        "deductionFromDate": "2019-09-06",
      |        "deductionToDate": "2019-10-05",
      |        "costOfMaterials": 35.11,
      |        "grossAmountPaid": 1457.11,
      |        "submissionDate": "2019-08-05",
      |        "submittedBy": "contractor"
      |      }
      |    ]
      |  }
      |]
      |}
      |""".stripMargin

  )

  val singleDeductionJson : JsValue = Json.parse{
    """
      |{
      | "cisDeductions" : [
      |  {
      |    "submissionId": "54759eb3c090d83494e2d804",
      |    "fromDate": "2019-04-06" ,
      |    "toDate": "2020-04-05",
      |    "contractorName": "Bovis",
      |    "employerRef": "BV40092",
      |    "periodData": [
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2019-06-06",
      |        "deductionToDate": "2019-07-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-01-14",
      |        "submittedBy": "customer"
      |      },
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2019-07-06",
      |        "deductionToDate": "2019-08-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-01-14",
      |        "submittedBy": "customer"
      |      }
      |    ]
      |  }
      |]
      |}
      |""".stripMargin
  }

  val singleDeductionJsonHateoas : JsValue = Json.parse{
    """
      |{
      | "cisDeductions" : [
      |  {
      |    "submissionId": "54759eb3c090d83494e2d804",
      |    "fromDate": "2019-04-06" ,
      |    "toDate": "2020-04-05",
      |    "contractorName": "Bovis",
      |    "employerRef": "BV40092",
      |    "periodData": [
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2019-06-06",
      |        "deductionToDate": "2019-07-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-01-14",
      |        "submittedBy": "customer"
      |      },
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2019-07-06",
      |        "deductionToDate": "2019-08-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-01-14",
      |        "submittedBy": "customer"
      |      }
      |    ],
      |    "links": [
      |                {
      |                    "href": "/deductions/cis/AA123456A/amendments/54759eb3c090d83494e2d804",
      |                    "method": "DELETE",
      |                    "rel": "delete-cis-deductions-for-subcontractor"
      |                },
      |                {
      |                    "href": "/deductions/cis/AA123456A/amendments/54759eb3c090d83494e2d804",
      |                    "method": "PUT",
      |                    "rel": "amend-cis-deductions-for-subcontractor"
      |                }
      |            ]
      |  }
      |],
      |    "links": [
      |        {
      |            "href": "/deductions/cis/AA123456A/current-position?fromDate=2019-04-06&toDate=2020-04-05&source=customer",
      |            "method": "GET",
      |            "rel": "self"
      |        },
      |        {
      |            "href": "/deductions/cis/AA123456A/amendments",
      |            "method": "POST",
      |            "rel": "create-cis-deductions-for-subcontractor"
      |        }
      |    ]
      |}
      |""".stripMargin
  }

  val singleDeductionJsonHateoasMissingOptionalField : JsValue = Json.parse{
    """
      |{
      | "cisDeductions" : [
      |  {
      |    "submissionId": "54759eb3c090d83494e2d804",
      |    "fromDate": "2019-04-06" ,
      |    "toDate": "2020-04-05",
      |    "contractorName": "Bovis",
      |    "employerRef": "BV40092",
      |    "periodData": [
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2019-06-06",
      |        "deductionToDate": "2019-07-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-01-14",
      |        "submittedBy": "customer"
      |      },
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2019-07-06",
      |        "deductionToDate": "2019-08-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-01-14",
      |        "submittedBy": "customer"
      |      }
      |    ],
      |    "links": [
      |                {
      |                    "href": "/deductions/cis/AA123456A/amendments/54759eb3c090d83494e2d804",
      |                    "method": "DELETE",
      |                    "rel": "delete-cis-deductions-for-subcontractor"
      |                },
      |                {
      |                    "href": "/deductions/cis/AA123456A/amendments/54759eb3c090d83494e2d804",
      |                    "method": "PUT",
      |                    "rel": "amend-cis-deductions-for-subcontractor"
      |                }
      |            ]
      |  }
      |],
      |    "links": [
      |        {
      |            "href": "/deductions/cis/AA123456A/current-position?fromDate=2019-04-06&toDate=2020-04-05",
      |            "method": "GET",
      |            "rel": "self"
      |        },
      |        {
      |            "href": "/deductions/cis/AA123456A/amendments",
      |            "method": "POST",
      |            "rel": "create-cis-deductions-for-subcontractor"
      |        }
      |    ]
      |}
      |""".stripMargin
  }

  val singleDeductionJsonHateoasNoId : JsValue = Json.parse{
    """
      |{
      | "cisDeductions" : [
      |  {
      |    "fromDate": "2019-04-06" ,
      |    "toDate": "2020-04-05",
      |    "contractorName": "Bovis",
      |    "employerRef": "BV40092",
      |    "periodData": [
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2019-06-06",
      |        "deductionToDate": "2019-07-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-01-14",
      |        "submittedBy": "contractor"
      |      },
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2019-07-06",
      |        "deductionToDate": "2019-08-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2020-01-14",
      |        "submittedBy": "contractor"
      |      }
      |    ]
      |  }
      |],
      |    "links": [
      |        {
      |            "href": "/deductions/cis/AA123456A/current-position?fromDate=2019-04-06&toDate=2020-04-05&source=customer",
      |            "method": "GET",
      |            "rel": "self"
      |        },
      |        {
      |            "href": "/deductions/cis/AA123456A/amendments",
      |            "method": "POST",
      |            "rel": "create-cis-deductions-for-subcontractor"
      |        }
      |    ]
      |}
      |""".stripMargin
  }

  val singleDeductionRequestJson =
    """
      |
      |""".stripMargin


  val errorJson =
    """
      |{
      | "cisDeductions" : [
      |  {
      |    "submissionId": "54759eb3c090d83494e2d804",
      |    "fromDate": "2019-04-06" ,
      |    "toDate": "2020-04-05",
      |    "employerRef": "BV40092",
      |    "periodData": [
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2019-04-06",
      |        "deductionToDate": "2019-05-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2019-04-03",
      |        "submittedBy": "contractor"
      |      },
      |      {
      |        "deductionAmount": 355.00,
      |        "deductionFromDate": "2019-05-06",
      |        "deductionToDate": "2019-06-05",
      |        "costOfMaterials": 35.00,
      |        "grossAmountPaid": 1457.00,
      |        "submissionDate": "2019-05-03",
      |        "submittedBy": "contractor"
      |      }
      |    ]
      |  }
      |]
      |}
      |""".stripMargin
}
