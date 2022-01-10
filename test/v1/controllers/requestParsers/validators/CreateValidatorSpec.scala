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

package v1.controllers.requestParsers.validators

import java.time.Year
import mocks.MockAppConfig
import org.joda.time.DateTime
import play.api.libs.json.Json
import support.UnitSpec
import v1.fixtures.CreateRequestFixtures._
import v1.models.errors._
import v1.models.request.create.CreateRawData

class CreateValidatorSpec extends UnitSpec{

  val nino = "AA123456A"
  val invalidNino = "GHFG197854"
  val nextTaxYearEnd = Year.now.getValue + 1
  val nextTaxYearStart = Year.now.getValue


  class SetUp extends MockAppConfig {
    val validator = new CreateValidator(mockAppConfig)
    MockedAppConfig.minTaxYearCisDeductions.returns("2019")
  }

  "running validation" should {
    "return no errors" when {
      "all the fields are submitted in a request" in new SetUp {

        validator.validate(CreateRawData(nino, requestJson)) shouldBe Nil
      }

      "an optional field is omitted in a request" in new SetUp {

        validator.validate(CreateRawData(nino, missingOptionalRequestJson)) shouldBe Nil
      }
    }
    "return errors" when {
      "invalid body type error" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, missingMandatoryFieldRequestJson)
        )
        result shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "invalid nino is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(invalidNino,requestJson)
        )
        result shouldBe List(NinoFormatError)
      }
      "invalid fromDate format is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidFromDateFormatRequestJson)
        )
        result shouldBe List(FromDateFormatError)
      }
      "invalid toDate format is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidToDateFormatRequestJson)
        )
        result shouldBe List(ToDateFormatError)
      }
      "invalid Deduction fromDate format is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidDeductionFromDateFormatRequestJson)
        )
        result shouldBe List(DeductionFromDateFormatError)
      }
      "invalid Deduction toDate format is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidDeductionToDateFormatRequestJson)
        )
        result shouldBe List(DeductionToDateFormatError)
      }
      "invalid deductionAmount too high is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidDeductionAmountTooHighRequestJson)
        )
        result shouldBe List(RuleDeductionAmountError)
      }
      "invalid deductionAmount negative is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidDeductionAmountNegativeRequestJson)
        )
        result shouldBe List(RuleDeductionAmountError)
      }
      "invalid CostOfMaterials too high is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidCostOfMaterialsTooHighRequestJson)
        )
        result shouldBe List(RuleCostOfMaterialsError)
      }
      "invalid CostOfMaterials negative is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidCostOfMaterialsNegativeRequestJson)
        )
        result shouldBe List(RuleCostOfMaterialsError)
      }
      "invalid GrossAmount too high is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidGrossAmountTooHighRequestJson)
        )
        result shouldBe List(RuleGrossAmountError)
      }
      "invalid GrossAmount negative is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidGrossAmountNegativeRequestJson)
        )
        result shouldBe List(RuleGrossAmountError)
      }
      "invalid toDate before fromDate is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, invalidToDateBeforeFromDateRequestJson)
        )
        result shouldBe List(RuleDateRangeInvalidError)
      }
      "invalid date range above the maximum threshold is provided" in new SetUp {
        private val result = validator.validate(
        CreateRawData(nino, requestBodyJsonErrorInvalidDateRangeMax)
        )
        result shouldBe List(RuleDateRangeInvalidError)
      }
      "invalid date range below the threshold is provided" in new SetUp {
        private val result = validator.validate(
        CreateRawData(nino, requestBodyJsonErrorInvalidDateRangeMin)
        )
        result shouldBe List(RuleDateRangeInvalidError)
      }
      "invalid date range before minimum tax year is provided" in new SetUp {
        private val result = validator.validate(
        CreateRawData(nino, requestBodyJsonErrorNotSupportedTaxYear)
        )
        result shouldBe List(RuleTaxYearNotSupportedError)
      }
      "tax year is dated beyond the latest completed tax year" in new SetUp {
        private val now = DateTime.now()
        val (start: String, end: String) = if((now.dayOfMonth().get() < 6 && now.monthOfYear().get() == 4) || now.monthOfYear().get() < 4) {
          (s"${nextTaxYearStart - 1}-04-06", s"${nextTaxYearEnd - 1}-04-05")
        } else {
          (s"${nextTaxYearStart}-04-06", s"${nextTaxYearEnd}-04-05")
        }
        validator.validate(
          CreateRawData(nino, Json.parse(
            s"""
              |{
              |  "fromDate": "$start" ,
              |  "toDate": "$end",
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
              |""".stripMargin))
        ) shouldBe List(RuleTaxYearNotEndedError)
      }
      "period falls outside the tax year identified by fromDate and toDate" in new SetUp {
        validator.validate(
          CreateRawData(nino, Json.parse(
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
              |""".stripMargin))
        ) shouldBe List(RuleUnalignedDeductionsPeriodError)
      }
      "period falls in the month after the tax year identified by fromDate and toDate" in new SetUp {
        validator.validate(
          CreateRawData(nino, Json.parse(
            """
              |{
              |  "fromDate": "2019-04-06" ,
              |  "toDate": "2020-04-05",
              |  "contractorName": "Bovis",
              |  "employerRef": "123/AB56797",
              |  "periodData": [
              |      {
              |      "deductionAmount": 355.00,
              |      "deductionFromDate": "2020-04-06",
              |      "deductionToDate": "2020-05-05",
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
              |""".stripMargin))
        ) shouldBe List(RuleUnalignedDeductionsPeriodError)
      }
      "period falls in the month before the tax year identified by fromDate and toDate" in new SetUp {
        validator.validate(
          CreateRawData(nino, Json.parse(
            """
              |{
              |  "fromDate": "2019-04-06" ,
              |  "toDate": "2020-04-05",
              |  "contractorName": "Bovis",
              |  "employerRef": "123/AB56797",
              |  "periodData": [
              |      {
              |      "deductionAmount": 355.00,
              |      "deductionFromDate": "2019-03-06",
              |      "deductionToDate": "2019-04-05",
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
              |""".stripMargin))
        ) shouldBe List(RuleUnalignedDeductionsPeriodError)
      }
      "deductionToDate precedes the deductionFromDate" in new SetUp {
        validator.validate(
          CreateRawData(nino, Json.parse(
            """
              |{
              |  "fromDate": "2019-04-06" ,
              |  "toDate": "2020-04-05",
              |  "contractorName": "Bovis",
              |  "employerRef": "123/AB56797",
              |  "periodData": [
              |      {
              |      "deductionAmount": 355.00,
              |      "deductionFromDate": "2020-08-05",
              |      "deductionToDate": "2020-07-06",
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
              |""".stripMargin))
        ) shouldBe List(RuleDeductionsDateRangeInvalidError)
      }
      "deductionToDate is thirteen months after the deductionFromDate" in new SetUp {
        validator.validate(
          CreateRawData(nino, Json.parse(
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
              |""".stripMargin))
        ) shouldBe List(RuleDeductionsDateRangeInvalidError)
      }
      "invalid employer reference format is provided" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, requestBodyJsonErrorInvalidEmpRef)
        )
        result shouldBe List(EmployerRefFormatError)
      }
    }
  }
}
