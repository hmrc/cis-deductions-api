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

import api.controllers.validators.Validator
import api.mocks.MockAppConfig
import api.models.domain.Nino
import api.models.errors._
import play.api.libs.json.JsValue
import support.UnitSpec
import v1.fixtures.CreateRequestFixtures._
import v1.models.request.create.CreateRequestData

class CreateValidatorFactorySpec extends UnitSpec {

  private implicit val correlationId: String = "1234"
  val nino                                   = "AA123456A"
  val invalidNino                            = "GHFG197854"

  class SetUp extends MockAppConfig {
    val validatorFactory = new CreateValidatorFactory(mockAppConfig)
    MockedAppConfig.minTaxYearCisDeductions.returns("2019").anyNumberOfTimes()
    val createRequestData: CreateRequestData         = CreateRequestData(Nino(nino), requestObj)
    val createRequestOptionalData: CreateRequestData = CreateRequestData(Nino(nino), missingOptionalRequestObj)

    def validator(nino: String, body: JsValue): Validator[CreateRequestData] =
      validatorFactory.validator(nino, body)

  }

  "running validation" should {
    "return no errors" when {
      "all the fields are submitted in a request" in new SetUp {

        private val result = validator(nino, requestJson).validateAndWrapResult()
        result shouldBe Right(createRequestData)
      }

      "an optional field is omitted in a request" in new SetUp {

        private val result = validator(nino, missingOptionalRequestJson).validateAndWrapResult()
        result shouldBe Right(createRequestOptionalData)
      }
    }
    "return errors" when {
      "invalid body type error" in new SetUp {
        private val result = validator(nino, missingMandatoryFieldRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }
      "an empty JSON period array is supplied as the request body" in new SetUp {
        private val result = validator(nino, missingPeriodDataRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }
      "invalid nino is provided" in new SetUp {
        private val result = validator(invalidNino, requestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
      "invalid fromDate format is provided" in new SetUp {
        private val result = validator(nino, invalidFromDateFormatRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
      }
      "invalid toDate format is provided" in new SetUp {
        private val result = validator(nino, invalidToDateFormatRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }
      "invalid Deduction fromDate format is provided" in new SetUp {
        private val result = validator(nino, invalidDeductionFromDateFormatRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, DeductionFromDateFormatError))
      }
      "invalid Deduction toDate format is provided" in new SetUp {
        private val result = validator(nino, invalidDeductionToDateFormatRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, DeductionToDateFormatError))
      }
      "invalid deductionAmount too high is provided" in new SetUp {
        private val result = validator(nino, invalidDeductionAmountTooHighRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleDeductionAmountError))
      }
      "invalid deductionAmount negative is provided" in new SetUp {
        private val result = validator(nino, invalidDeductionAmountNegativeRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleDeductionAmountError))
      }
      "invalid CostOfMaterials too high is provided" in new SetUp {
        private val result = validator(nino, invalidCostOfMaterialsTooHighRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleCostOfMaterialsError))
      }
      "invalid CostOfMaterials negative is provided" in new SetUp {
        private val result = validator(nino, invalidCostOfMaterialsNegativeRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleCostOfMaterialsError))
      }
      "invalid GrossAmount too high is provided" in new SetUp {
        private val result = validator(nino, invalidGrossAmountTooHighRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleGrossAmountError))
      }
      "invalid GrossAmount negative is provided" in new SetUp {
        private val result = validator(nino, invalidGrossAmountNegativeRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleGrossAmountError))
      }
      "invalid toDate before fromDate is provided" in new SetUp {
        private val result = validator(nino, invalidToDateBeforeFromDateRequestJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleDateRangeInvalidError))
      }
      "invalid date range above the maximum threshold is provided" in new SetUp {
        private val result = validator(nino, requestBodyJsonErrorInvalidDateRangeMax).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleDateRangeInvalidError))
      }
      "invalid date range below the threshold is provided" in new SetUp {
        private val result = validator(nino, requestBodyJsonErrorInvalidDateRangeMin).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleDateRangeInvalidError))
      }
      "invalid date range before minimum tax year is provided" in new SetUp {
        private val result = validator(nino, requestBodyJsonErrorNotSupportedTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
      "invalid employer reference format is provided" in new SetUp {
        private val result = validator(nino, requestBodyJsonErrorInvalidEmpRef).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, EmployerRefFormatError))
      }
    }
  }

}
