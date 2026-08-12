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
import api.utils.UnitSpec
import config.MockCisDeductionsApiConfig
import models.errors.*
import play.api.libs.json.JsValue
import v3.fixtures.AmendRequestFixtures.*
import v3.models.domain.SubmissionId
import v3.models.errors.CisDeductionsApiCommonErrors.{DeductionFromDateFormatError, DeductionToDateFormatError}
import v3.models.request.amend.AmendRequestData

import java.time.LocalDate

class AmendValidatorFactorySpec extends UnitSpec with MockCisDeductionsApiConfig {

  private given correlationId: String   = "1234"
  private val validNino: String         = "AA123456A"
  private val validSubmissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  "running validation" should {
    "return no errors" when {
      "a full valid request with all the fields is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator().validateAndWrapResult()

        result shouldBe Right(
          AmendRequestData(Nino(validNino), SubmissionId(validSubmissionId), TaxYear.fromMtd("2019-20"), amendRequestObj)
        )
      }

      "a minimum valid request with only mandatory fields is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = requestJsonWithoutOptionalValues).validateAndWrapResult()

        result shouldBe Right(
          AmendRequestData(Nino(validNino), SubmissionId(validSubmissionId), TaxYear.fromMtd("2019-20"), amendMissingOptionalRequestObj)
        )
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(nino = "GHFG197854").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return SubmissionIdFormatError error" when {
      "an invalid invalid submission ID is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(submissionId = "contractor1").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, SubmissionIdFormatError))
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "mandatory fields are missing" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = missingMandatoryFieldRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/periodData/0/deductionAmount")))
      }

      "an empty periodData array is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = emptyPeriodDataJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/periodData")))
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "a date range for a tax year before the minimum supported tax year is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = requestJsonErrorTaxYearNotSupported).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }

    "return DeductionFromDateFormatError error" when {
      "an invalid deduction from date format is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = requestJsonErrorDeductionFromDate).validateAndWrapResult()

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
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = requestJsonErrorDeductionToDate).validateAndWrapResult()

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
      "a deduction to date that is before deduction from date is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = requestJsonErrorDeductionToDateBeforeFromDate).validateAndWrapResult()

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

    "return RuleDeductionsDateRangeInvalidError error" when {
      "a deduction period that does not align from the 6th of one month to the 5th of the following month is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = requestJsonErrorDeductionPeriodNotAligned).validateAndWrapResult()

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
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = requestJsonErrorDuplicateDeductionPeriods).validateAndWrapResult()

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

    "return RuleDeductionAmountError error" when {
      "a deduction amount that is too high is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = requestJsonErrorDeductionAmountTooHigh).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleDeductionAmountError.withPath("/periodData/0/deductionAmount")))
      }

      "a deduction amount that is negative is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = requestJsonErrorDeductionAmountNegative).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleDeductionAmountError.withPath("/periodData/0/deductionAmount")))
      }
    }

    "return RuleCostOfMaterialsError error" when {
      "a cost of materials that is too high is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = requestJsonErrorCostOfMaterialsTooHigh).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleCostOfMaterialsError.withPath("/periodData/0/costOfMaterials")))
      }

      "a cost of materials that is negative is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = requestJsonErrorCostOfMaterialsNegative).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleCostOfMaterialsError.withPath("/periodData/1/costOfMaterials")))
      }
    }

    "return RuleGrossAmountError error" when {
      "a gross amount paid that is too high is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = requestJsonErrorGrossAmountPaidTooHigh).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleGrossAmountError.withPath("/periodData/0/grossAmountPaid")))
      }

      "a gross amount paid that is negative is supplied" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = requestJsonErrorGrossAmountPaidNegative).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleGrossAmountError.withPath("/periodData/0/grossAmountPaid")))
      }
    }

    "return multiple errors" when {
      "multiple date range validation rules are violated" in new Test {
        val result: Either[ErrorWrapper, AmendRequestData] = validator(body = requestJsonErrorDatesOutsideSupportedRange).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(
              List(
                DeductionFromDateFormatError.withPath("/periodData/0/deductionFromDate"),
                DeductionToDateFormatError.withPath("/periodData/1/deductionToDate")
              )
            )
          )
        )
      }
    }
  }

  private trait Test {
    MockedCisDeductionApiConfig.minTaxYearCisDeductions.returns(TaxYear.starting(2019)).anyNumberOfTimes()

    private val validatorFactory: AmendValidatorFactory = new AmendValidatorFactory(mockCisDeductionApiConfig)

    protected def validator(nino: String = validNino,
                            submissionId: String = validSubmissionId,
                            body: JsValue = requestJson): Validator[AmendRequestData] =
      validatorFactory.validator(nino, submissionId, body)

  }

}
