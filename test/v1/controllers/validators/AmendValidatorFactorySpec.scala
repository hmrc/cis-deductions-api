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

import play.api.libs.json.JsValue
import shared.UnitSpec
import shared.models.domain.{Nino, SubmissionId, TaxYear}
import shared.models.errors._
import v1.fixtures.AmendRequestFixtures._
import v1.models.errors.CisDeductionsApiCommonErrors.{DeductionFromDateFormatError, DeductionToDateFormatError}
import v1.models.request.amend.AmendRequestData

class AmendValidatorFactorySpec extends UnitSpec {

  private implicit val correlationId: String = "1234"
  private val validNino                      = "AA123456A"
  private val validId                        = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val validatorFactory = new AmendValidatorFactory()

  private def validator(nino: String, submissionId: String, body: JsValue) =
    validatorFactory.validator(nino, submissionId, body)

  "running amend validation" should {
    "return no errors" when {
      "a valid request is supplied" in {

        val amendBodyTaxYear    = "2019-20"
        val amendRequestDataObj = AmendRequestData(Nino(validNino), SubmissionId(validId), TaxYear.fromMtd(amendBodyTaxYear), amendRequestObj)
        val result              = validator(validNino, validId, requestJson).validateAndWrapResult()
        result shouldBe Right(amendRequestDataObj)
      }
    }
    "return a single error" when {
      "an invalid nino is supplied" in {
        val result = validator("23456A", validId, requestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
      "an invalid submission id is supplied" in {
        val result = validator(validNino, "contractor1", requestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, SubmissionIdFormatError))
      }
    }
    "return multiple errors" when {
      "multiple wrong fields are supplied" in {
        val result = validator("2sbt3456A", "idcontract123", requestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, SubmissionIdFormatError))))
      }
    }
    "return a single error" when {
      "invalid body type error" in new AmendValidatorFactory {
        private val result = validatorFactory.validator(validNino, validId, missingMandatoryFieldRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/periodData/0/deductionAmount")))
      }
      "an empty JSON period array is supplied as the request body" in new AmendValidatorFactory {
        private val result = validatorFactory.validator(validNino, validId, emptyPeriodDataJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "invalid request body Deduction fromDate format is provided" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidDeductionFromDateFormatRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, DeductionFromDateFormatError))
      }
      "invalid request body Deduction toDate and fromDate are not within range" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidRangeDeductionToDateFromDateFormatRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(DeductionFromDateFormatError, DeductionToDateFormatError))))
      }
      "invalid request body Deduction toDate format is provided" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidDeductionToDateFormatRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, DeductionToDateFormatError))
      }
      "invalid request body deductionAmount too high is provided" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidDeductionAmountTooHighRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleDeductionAmountError))
      }
      "invalid request body deductionAmount negative is provided" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidDeductionAmountNegativeRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleDeductionAmountError))
      }
      "invalid request body CostOfMaterials too high is provided" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidCostOfMaterialsTooHighRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleCostOfMaterialsError))
      }
      "invalid request body CostOfMaterials negative is provided" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidCostOfMaterialsNegativeRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleCostOfMaterialsError))
      }
      "invalid request body GrossAmount too high is provided" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidGrossAmountTooHighRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleGrossAmountError))
      }
      "invalid request body GrossAmount negative is provided" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidGrossAmountNegativeRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleGrossAmountError))
      }
    }
  }

}
