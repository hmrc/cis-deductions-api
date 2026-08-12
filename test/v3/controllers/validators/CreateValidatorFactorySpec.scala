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

package v3.controllers.validators

import api.controllers.validators.Validator
import api.models.domain.{DateRange, Nino, TaxYear}
import api.models.errors.*
import api.models.utils.JsonErrorValidators
import api.utils.UnitSpec
import config.MockCisDeductionsApiConfig
import models.errors.*
import play.api.libs.json.{JsString, JsValue}
import v3.fixtures.CreateRequestFixtures.*
import v3.models.errors.CisDeductionsApiCommonErrors.{DeductionFromDateFormatError, DeductionToDateFormatError}
import v3.models.request.create.{CreateBody, CreateRequestData}

import java.time.LocalDate

class CreateValidatorFactorySpec extends UnitSpec with MockCisDeductionsApiConfig with JsonErrorValidators {

  private given correlationId: String = "1234"
  private val validNino: String       = "AA123456A"

  "running validation" should {
    "return no errors" when {
      "a full valid request with all the fields is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator().validateAndWrapResult()

        result shouldBe Right(CreateRequestData(Nino(validNino), parsedRequestData))
      }

      "a minimum valid request with only mandatory fields is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = missingOptionalRequestJson).validateAndWrapResult()

        result shouldBe Right(CreateRequestData(Nino(validNino), parsedRequestDataMissingOptional))
      }

      "a supplied date range is for a tax year that has not ended and temporal validation is disabled" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] =
          validator(body = requestJsonCurrentTaxYear, temporalValidationEnabled = false).validateAndWrapResult()

        result shouldBe Right(CreateRequestData(Nino(validNino), requestJsonCurrentTaxYear.as[CreateBody]))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(nino = "GHFG197854").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "mandatory fields are missing" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = missingMandatoryFieldRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/periodData/0/deductionAmount")))
      }

      "an empty periodData array is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = emptyPeriodDataJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/periodData")))
      }
    }

    "return FromDateFormatError error" when {
      "an invalid from date format is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorFromDate).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError.withPath("/fromDate")))
      }
    }

    "return ToDateFormatError error" when {
      "an invalid to date format is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorToDate).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError.withPath("/toDate")))
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "a date range for a tax year before the minimum supported tax year is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorTaxYearNotSupported).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }

    "return RuleTaxYearNotEndedError error" when {
      "a supplied date range is for a tax year that has not ended and temporal validation is enabled" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonCurrentTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotEndedError))
      }
    }

    "return DeductionFromDateFormatError error" when {
      "an invalid deduction from date format is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorDeductionFromDate).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            DeductionFromDateFormatError.withPaths(
              List(
                "/periodData/0/deductionFromDate",
                "/periodData/1/deductionFromDate"
              )
            )
          )
        )
      }
    }

    "return DeductionToDateFormatError error" when {
      "an invalid deduction to date format is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorDeductionToDate).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            DeductionToDateFormatError.withPaths(
              List(
                "/periodData/0/deductionToDate",
                "/periodData/1/deductionToDate"
              )
            )
          )
        )
      }
    }

    "return RuleDateRangeInvalidError error" when {
      "a to date that is before from date is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorToDateBeforeFromDate).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleDateRangeInvalidError))
      }

      "a date range above the maximum threshold is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorDateRangeMax).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleDateRangeInvalidError))
      }

      "a date that is not a complete tax year is supplied" should {
        behave like returnDateRangeInvalidError("2019-04-06", "2020-04-06", "to after tax year end")
        behave like returnDateRangeInvalidError("2019-04-06", "2020-04-04", "to before tax year end")
        behave like returnDateRangeInvalidError("2019-04-05", "2020-04-05", "from before tax year start")
        behave like returnDateRangeInvalidError("2019-04-07", "2020-04-05", "from after tax year start")
        behave like returnDateRangeInvalidError("2019-04-06", "2021-04-05", "different tax year")

        def returnDateRangeInvalidError(fromDate: String, toDate: String, clue: String): Unit =
          s"return RuleDateRangeInvalidError for $fromDate to $toDate" in new Test {
            withClue(clue) {
              validator(body = requestJsonWithDates(fromDate, toDate))
                .validateAndWrapResult() shouldBe
                Left(ErrorWrapper(correlationId, RuleDateRangeInvalidError))
            }
          }
      }

      "a deduction to date that is before deduction from date is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorDeductionToDateBeforeFromDate).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleDateRangeInvalidError.withPaths(
              List(
                "/periodData/0",
                "/periodData/1"
              )
            )
          )
        )
      }
    }

    "return RuleUnalignedDeductionsPeriodError error" when {
      "a deduction period that is outside the supplied tax year is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorDeductionPeriodsOutsideTaxYear).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleUnalignedDeductionsPeriodError.withPaths(
              List(
                "/periodData/0",
                "/periodData/1"
              )
            )
          )
        )
      }
    }

    "return RuleDeductionsDateRangeInvalidError error" when {
      "a deduction period that does not align from the 6th of one month to the 5th of the following month is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorDeductionPeriodNotAligned).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleDeductionsDateRangeInvalidError.withPaths(
              List(
                "/periodData/0/deductionFromDate",
                "/periodData/0/deductionToDate",
                "/periodData/1/deductionFromDate",
                "/periodData/1/deductionToDate"
              )
            )
          )
        )
      }
    }

    "return RuleDuplicatePeriodError error" when {
      "duplicate deduction periods are supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorDuplicateDeductionPeriods).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleDuplicatePeriodError.forDuplicatedPeriod(
              DateRange(LocalDate.parse("2019-06-06"), LocalDate.parse("2019-07-05")),
              List("/periodData/0", "/periodData/1")
            )
          )
        )
      }
    }

    "return ContractorNameFormatError error" when {
      "an invalid contractor name format is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(
          body = requestJson.update("/contractorName", JsString("a" * 106))
        ).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ContractorNameFormatError.withPath("/contractorName")))
      }
    }

    "return EmployerRefFormatError error" when {
      "an invalid employer reference format is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorEmpRef).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, EmployerRefFormatError.withPath("/employerRef")))
      }
    }

    "return RuleDeductionAmountError error" when {
      "a deduction amount that is too high is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorDeductionAmountTooHigh).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleDeductionAmountError.withPath("/periodData/0/deductionAmount")))
      }

      "a deduction amount that is negative is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorDeductionAmountNegative).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleDeductionAmountError.withPath("/periodData/0/deductionAmount")))
      }
    }

    "return RuleCostOfMaterialsError error" when {
      "a cost of materials that is too high is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorCostOfMaterialsTooHigh).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleCostOfMaterialsError.withPath("/periodData/0/costOfMaterials")))
      }

      "a cost of materials that is negative is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorCostOfMaterialsNegative).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleCostOfMaterialsError.withPath("/periodData/1/costOfMaterials")))
      }
    }

    "return RuleGrossAmountError error" when {
      "a gross amount paid that is too high is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorGrossAmountPaidTooHigh).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleGrossAmountError.withPath("/periodData/0/grossAmountPaid")))
      }

      "a gross amount paid that is negative is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorGrossAmountPaidNegative).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleGrossAmountError.withPath("/periodData/0/grossAmountPaid")))
      }
    }

    "return multiple errors" when {
      "multiple date range validation rules are violated" in new Test {
        val result: Either[ErrorWrapper, CreateRequestData] = validator(body = requestJsonErrorDatesOutsideSupportedRange).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(
              List(
                DeductionFromDateFormatError.withPath("/periodData/0/deductionFromDate"),
                DeductionToDateFormatError.withPath("/periodData/1/deductionToDate"),
                FromDateFormatError.withPath("/fromDate"),
                ToDateFormatError.withPath("/toDate")
              )
            )
          )
        )
      }
    }
  }

  private trait Test {
    MockedCisDeductionApiConfig.minTaxYearCisDeductions.returns(TaxYear.starting(2019)).anyNumberOfTimes()

    private val validatorFactory: CreateValidatorFactory = new CreateValidatorFactory(mockCisDeductionApiConfig)

    protected def validator(nino: String = validNino,
                            body: JsValue = requestJson,
                            temporalValidationEnabled: Boolean = true): Validator[CreateRequestData] =
      validatorFactory.validator(nino, body, temporalValidationEnabled)

  }

}
