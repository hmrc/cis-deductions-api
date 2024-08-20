/*
 * Copyright 2023 HM Revenue & Customs
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

package v1.controllers.validators

import config.MockCisDeductionApiConfig
import models.domain.CisSource
import models.errors.{RuleMissingFromDateError, RuleSourceInvalidError}
import shared.config.MockAppConfig
import shared.controllers.validators.Validator
import shared.models.domain.{DateRange, Nino, TaxYear}
import shared.models.errors._
import shared.utils.UnitSpec
import v1.models.request.retrieve.RetrieveRequestData

import java.time.LocalDate

class RetrieveValidatorFactorySpec extends UnitSpec with MockAppConfig {
  private implicit val correlationId: String = "1234"
  private val nino                           = "AA123456A"
  private val invalidNino                    = "GHFG197854"

  private val fromDateStr = "2019-04-06"
  private val toDateStr   = "2020-04-05"

  private val fromDate: LocalDate = LocalDate.parse(fromDateStr)
  private val toDate: LocalDate   = LocalDate.parse(toDateStr)

  private val dateRange: DateRange = DateRange(fromDate, toDate)

  "running validation" should {
    "return no errors" when {
      "all query parameters are passed in the request" in new Test {
        val result: Either[ErrorWrapper, RetrieveRequestData] =
          validator(nino, Some(fromDateStr), Some(toDateStr), Some("all")).validateAndWrapResult()
        result shouldBe Right(RetrieveRequestData(Nino(nino), dateRange, CisSource.`all`))
      }

      "an optional field returns None" in new Test {
        val result: Either[ErrorWrapper, RetrieveRequestData] =
          validator(nino, Some(fromDateStr), Some(toDateStr), None).validateAndWrapResult()
        result shouldBe Right(RetrieveRequestData(Nino(nino), dateRange, CisSource.`all`))
      }
    }

    "return errors" when {
      "invalid nino and source data is passed in the request" in new Test {
        val result: Either[ErrorWrapper, RetrieveRequestData] =
          validator(invalidNino, Some(fromDateStr), Some(toDateStr), Some("All")).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper("1234", BadRequestError, Some(List(NinoFormatError, RuleSourceInvalidError))))
      }

      "invalid dates are provided in the request" in new Test {
        val result: Either[ErrorWrapper, RetrieveRequestData] =
          validator(nino, Some("2018-04-06"), Some("2020-04-05"), Some("contractor")).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper("1234", RuleDateRangeInvalidError))
      }

      "the from and to date are not provided" in new Test {
        val result: Either[ErrorWrapper, RetrieveRequestData] = validator(nino, None, None, Some("customer")).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper("1234", BadRequestError, Some(List(RuleMissingFromDateError, RuleMissingToDateError))))
      }

      "the from & to date are not in the correct format" in new Test {
        val result: Either[ErrorWrapper, RetrieveRequestData] =
          validator(nino, Some("last week"), Some("this week"), Some("customer")).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper("1234", BadRequestError, Some(List(FromDateFormatError, ToDateFormatError))))
      }

      "given a date that is not a complete tax year" must {
        behave like returnDateRangeInvalidError("2019-04-06", "2020-04-06", "to after tax year end")
        behave like returnDateRangeInvalidError("2019-04-06", "2020-04-04", "to before tax year end")
        behave like returnDateRangeInvalidError("2019-04-05", "2020-04-05", "from before tax year start")
        behave like returnDateRangeInvalidError("2019-04-07", "2020-04-05", "from after tax year start")
        behave like returnDateRangeInvalidError("2019-04-06", "2021-04-05", "different tax year")

        def returnDateRangeInvalidError(fromDate: String, toDate: String, clue: String): Unit =
          s"return RuleDateRangeInvalidError for $fromDate to $toDate" in new Test {
            withClue(clue) {
              validator(nino, Some(fromDate), Some(toDate), Some("customer")).validateAndWrapResult() shouldBe
                Left(ErrorWrapper(correlationId, RuleDateRangeInvalidError))
            }
          }
      }
    }
  }

  private class Test extends MockAppConfig with MockCisDeductionApiConfig {
    MockedCisDeductionApiConfig.minTaxYearCisDeductions.returns(TaxYear.starting(2020))
    private val validatorFactory: RetrieveValidatorFactory = new RetrieveValidatorFactory

    protected def validator(nino: String, fromDate: Option[String], toDate: Option[String], source: Option[String]): Validator[RetrieveRequestData] =
      validatorFactory.validator(nino, fromDate, toDate, source)

  }

}
