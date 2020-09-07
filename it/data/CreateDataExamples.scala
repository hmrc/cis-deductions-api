package data

import play.api.libs.json.Json

object CreateDataExamples {

  val emptyRequest = Json.parse(
    s"""
      |{
      |}
      |""".stripMargin
  )

  val deductionsResponseBody = Json.parse(
    """
      |{
      |   "submissionId":"someResponse",
      |   "links":[
      |      {
      |         "href":"/individuals/deductions/cis/AA123456A/current-position?fromDate=2020-04-06&toDate=2021-04-05",
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
      |  "fromDate": "2020-04-06" ,
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
        """.stripMargin
  )
  val requestBodyJsonErrorFromDate = Json.parse(
    """
      |{
      |  "fromDate": "04-06-2020" ,
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
        """.stripMargin
  )
  val requestBodyJsonErrorToDate = Json.parse(
    """
      |{
      |  "fromDate": "2020-04-06" ,
      |  "toDate": "04-05-2021",
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
  val requestBodyJsonErrorDeductionFromDate = Json.parse(
    """
      |{
      |  "fromDate": "2020-04-06" ,
      |  "toDate": "2021-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "06-06-2020",
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
  val requestBodyJsonErrorDeductionToDate = Json.parse(
    """
      |{
      |  "fromDate": "2020-04-06" ,
      |  "toDate": "2021-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
      |      "deductionAmount": 355.00,
      |      "deductionFromDate": "2020-06-06",
      |      "deductionToDate": "07-05-2020",
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
  val requestBodyJsonErrorTaxYearNotSupported = Json.parse(
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
  val requestInvalidEmpRef = Json.parse(
    """
      |{
      |  "fromDate": "2020-04-06" ,
      |  "toDate": "2021-04-05",
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

  val requestRuleDeductionAmountJson = Json.parse(
    """
      |{
      |  "fromDate": "2020-04-06" ,
      |  "toDate": "2021-04-05",
      |  "contractorName": "Bovis",
      |  "employerRef": "123/AB56797",
      |  "periodData": [
      |      {
      |      "deductionAmount": -355.00,
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

  val requestInvalidRuleCostOfMaterialsJson = Json.parse(
    """
      |{
      |  "fromDate": "2020-04-06" ,
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
      |  "fromDate": "2020-04-06" ,
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
      |      "grossAmountPaid": -1457.00
      |    }
      |  ]
      |}
        """.stripMargin
  )

  val requestInvalidDateRangeJson = Json.parse(
    """
      |{
      |  "fromDate": "2020-04-06" ,
      |  "toDate": "2022-04-05",
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
}