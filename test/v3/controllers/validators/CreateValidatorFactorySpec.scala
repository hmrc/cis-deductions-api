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

package v3.controllers.validators

import config.MockCisDeductionsApiConfig
import models.errors.{EmployerRefFormatError, RuleCostOfMaterialsError, RuleDeductionAmountError, RuleGrossAmountError}
import play.api.libs.json.JsValue
import shared.config.MockSharedAppConfig
import shared.controllers.validators.Validator
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.utils.UnitSpec
import v3.fixtures.CreateRequestFixtures._
import v3.models.errors.CisDeductionsApiCommonErrors.{DeductionFromDateFormatError, DeductionToDateFormatError}
import v3.models.request.create
import v3.models.request.create.CreateRequestData

class CreateValidatorFactorySpec extends UnitSpec {

  private implicit val correlationId: String = "1234"
  val nino                                   = "AA123456A"
  val invalidNino                            = "GHFG197854"

  "running validation" should {
    "return no errors" when {
      "all the fields are submitted in a request" in new Test {
        private val result = validator(nino, requestJson).validateAndWrapResult()
        result shouldBe Right(createRequestData)
      }

      "an optional field is omitted in a request" in new Test {
        private val result = validator(nino, missingOptionalRequestJson).validateAndWrapResult()
        result shouldBe Right(createRequestOptionalData)
      }
    }
    "return errors" when {
      "invalid body type error" in new Test {
        private val result = validator(nino, missingMandatoryFieldRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/periodData/0/deductionAmount")))
      }

      "an empty JSON period array is supplied as the request body" in new Test {
        private val result = validator(nino, missingPeriodDataRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "invalid nino is provided" in new Test {
        private val result = validator(invalidNino, requestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "invalid fromDate format is provided" in new Test {
        private val result = validator(nino, invalidFromDateFormatRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }

      "invalid toDate format is provided" in new Test {
        private val result = validator(nino, invalidToDateFormatRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }

      "invalid Deduction fromDate format is provided" in new Test {
        private val result = validator(nino, invalidDeductionFromDateFormatRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, DeductionFromDateFormatError))
      }

      "invalid Deduction toDate format is provided" in new Test {
        private val result = validator(nino, invalidDeductionToDateFormatRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, DeductionToDateFormatError))
      }

      "invalid Deduction fromDate and toDate format is provided" in new Test {
        private val result = validator(nino, invalidDeductionFromAndToDateFormatRequestJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(DeductionFromDateFormatError, DeductionToDateFormatError, FromDateFormatError, ToDateFormatError))))
      }

      "invalid deductionAmount too high is provided" in new Test {
        private val result = validator(nino, invalidDeductionAmountTooHighRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleDeductionAmountError))
      }

      "invalid deductionAmount negative is provided" in new Test {
        private val result = validator(nino, invalidDeductionAmountNegativeRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleDeductionAmountError))
      }

      "invalid CostOfMaterials too high is provided" in new Test {
        private val result = validator(nino, invalidCostOfMaterialsTooHighRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleCostOfMaterialsError))
      }

      "invalid CostOfMaterials negative is provided" in new Test {
        private val result = validator(nino, invalidCostOfMaterialsNegativeRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleCostOfMaterialsError))
      }

      "invalid GrossAmount too high is provided" in new Test {
        private val result = validator(nino, invalidGrossAmountTooHighRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleGrossAmountError))
      }

      "invalid GrossAmount negative is provided" in new Test {
        private val result = validator(nino, invalidGrossAmountNegativeRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleGrossAmountError))
      }

      "invalid toDate before fromDate is provided" in new Test {
        private val result = validator(nino, invalidToDateBeforeFromDateRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleDateRangeInvalidError))
      }

      "given a date range that's too large (above maximum threshold)" in new Test {
        private val result = validator(nino, requestBodyJsonErrorInvalidDateRangeMax).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleDateRangeInvalidError))
      }

      // Added to catch bug introduced recently (ok in commit 57177ca6) whereby a range greater than a tax year
      // but still spanning just two years we seen as valid
      "given a date that is not a complete tax year" must {
        behave like returnDateRangeInvalidError("2019-04-06", "2020-04-06", "to after tax year end")
        behave like returnDateRangeInvalidError("2019-04-06", "2020-04-04", "to before tax year end")
        behave like returnDateRangeInvalidError("2019-04-05", "2020-04-05", "from before tax year start")
        behave like returnDateRangeInvalidError("2019-04-07", "2020-04-05", "from after tax year start")
        behave like returnDateRangeInvalidError("2019-04-06", "2021-04-05", "different tax year")

        def returnDateRangeInvalidError(fromDate: String, toDate: String, clue: String): Unit =
          s"return RuleDateRangeInvalidError for $fromDate to $toDate" in new Test {
            withClue(clue) {
              validator(nino, requestBodyJsonWith(fromDate, toDate))
                .validateAndWrapResult() shouldBe
                Left(ErrorWrapper(correlationId, RuleDateRangeInvalidError))
            }
          }
      }

      "invalid date range before minimum tax year is provided" in new Test {
        private val result = validator(nino, requestBodyJsonErrorNotSupportedTaxYear).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "invalid employer reference format is provided" in new Test {
        private val result = validator(nino, requestBodyJsonErrorInvalidEmpRef).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, EmployerRefFormatError))
      }
    }
  }

  private class Test extends MockSharedAppConfig with MockCisDeductionsApiConfig {
    MockedCisDeductionApiConfig.minTaxYearCisDeductions.returns(TaxYear.starting(2019)).anyNumberOfTimes()
    private val validatorFactory = new CreateValidatorFactory(mockCisDeductionApiConfig)

    protected val createRequestData: CreateRequestData         = create.CreateRequestData(Nino(nino), parsedRequestData)
    protected val createRequestOptionalData: CreateRequestData = create.CreateRequestData(Nino(nino), parsedRequestDataMissingOptional)

    protected def validator(nino: String, body: JsValue): Validator[CreateRequestData] =
      validatorFactory.validator(nino, body)

  }

}
