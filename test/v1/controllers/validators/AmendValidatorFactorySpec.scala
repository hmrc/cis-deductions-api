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

import models.errors.{RuleCostOfMaterialsError, RuleDeductionAmountError, RuleGrossAmountError, SubmissionIdFormatError}
import play.api.libs.json.JsValue
import shared.utils.UnitSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import v1.fixtures.AmendRequestFixtures._
import v1.models.domain.SubmissionId
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
      "given a valid request" in {
        val amendBodyTaxYear    = "2019-20"
        val amendRequestDataObj = AmendRequestData(Nino(validNino), SubmissionId(validId), TaxYear.fromMtd(amendBodyTaxYear), amendRequestObj)
        val result              = validator(validNino, validId, requestJson).validateAndWrapResult()
        result shouldBe Right(amendRequestDataObj)
      }

      "given a valid request with none of the optional values" in {
        val amendBodyTaxYear = "2019-20"
        val amendRequestDataObj =
          AmendRequestData(Nino(validNino), SubmissionId(validId), TaxYear.fromMtd(amendBodyTaxYear), amendMissingOptionalRequestObj)
        val result = validator(validNino, validId, requestJsonWithoutOptionalValues).validateAndWrapResult()
        result shouldBe Right(amendRequestDataObj)
      }
    }

    "return a single error" when {
      "given an invalid nino" in {
        val result = validator("23456A", validId, requestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given an invalid submission ID" in {
        val result = validator(validNino, "contractor1", requestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, SubmissionIdFormatError))
      }
    }

    "return multiple errors" when {
      "given multiple wrong fields" in {
        val result = validator("2sbt3456A", "idcontract123", requestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, SubmissionIdFormatError))))
      }
    }

    "return a single error" when {
      "invalid body type error" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, missingMandatoryFieldRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/periodData/0/deductionAmount")))
      }

      "given an empty JSON period array in the request body" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, emptyPeriodDataJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "given an invalid request body Deduction fromDate format" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidDeductionFromDateFormatRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, DeductionFromDateFormatError))
      }

      "invalid request body Deduction toDate and fromDate are not within range" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidRangeDeductionToDateFromDateFormatRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(DeductionFromDateFormatError, DeductionToDateFormatError))))
      }

      "given an invalid request body Deduction toDate format" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidDeductionToDateFormatRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, DeductionToDateFormatError))
      }

      "given an invalid request body deductionAmount too high" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidDeductionAmountTooHighRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleDeductionAmountError))
      }

      "given an invalid request body deductionAmount negative" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidDeductionAmountNegativeRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleDeductionAmountError))
      }

      "given an invalid request body CostOfMaterials too high" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidCostOfMaterialsTooHighRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleCostOfMaterialsError))
      }

      "given an invalid request body CostOfMaterials negative" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidCostOfMaterialsNegativeRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleCostOfMaterialsError))
      }

      "given an invalid request body GrossAmount too high" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidGrossAmountTooHighRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleGrossAmountError))
      }

      "given an invalid request body GrossAmount negative" in new AmendValidatorFactory {
        private val result = validator(validNino, validId, invalidGrossAmountNegativeRequestJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleGrossAmountError))
      }
    }
  }

}
