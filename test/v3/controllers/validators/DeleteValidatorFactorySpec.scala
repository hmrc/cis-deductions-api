/*
 * Copyright 2025 HM Revenue & Customs
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
import models.errors.SubmissionIdFormatError
import shared.config.MockSharedAppConfig
import shared.controllers.validators.Validator
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.utils.UnitSpec
import v3.models.domain.SubmissionId
import v3.models.request.delete.DeleteRequestData

class DeleteValidatorFactorySpec extends UnitSpec with MockSharedAppConfig with MockCisDeductionsApiConfig {

  private given correlationId: String = "1234"

  private val validNino         = "AA123456A"
  private val validSubmissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val rawTaxYear        = "2023-24"

  "running a delete validation" should {

    "return no errors" when {
      "given a valid request" in new Test {
        val result: Either[ErrorWrapper, DeleteRequestData] = validator(validNino, validSubmissionId, rawTaxYear).validateAndWrapResult()
        result shouldBe Right(DeleteRequestData(Nino(validNino), SubmissionId(validSubmissionId), TaxYear.fromMtd(rawTaxYear)))
      }
    }

    "return a single error" when {
      "given an invalid nino" in new Test {
        val result: Either[ErrorWrapper, DeleteRequestData] = validator("23456A", validSubmissionId, rawTaxYear).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given an invalid submission id" in new Test {
        val result: Either[ErrorWrapper, DeleteRequestData] = validator(validNino, "contractor1", rawTaxYear).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, SubmissionIdFormatError))
      }

      "given an invalid tax year" in new Test {
        val result: Either[ErrorWrapper, DeleteRequestData] = validator(validNino, validSubmissionId, "2023-25").validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return multiple errors" when {
      "given multiple wrong fields" in new Test {
        val result: Either[ErrorWrapper, DeleteRequestData] =
          validator("2sbt3456A", "idcontract123", "bad-tax-year-format").validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, SubmissionIdFormatError, TaxYearFormatError))))
      }
    }
  }

  private class Test {
    private val validatorFactory = new DeleteValidatorFactory

    protected def validator(nino: String, submissionId: String, taxYear: String): Validator[DeleteRequestData] =
      validatorFactory.validator(nino, submissionId, taxYear)

  }

}
