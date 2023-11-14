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

import api.mocks.MockAppConfig
import shared.UnitSpec
import shared.controllers.validators.Validator
import shared.models.domain.{Nino, SubmissionId, TaxYear}
import shared.models.errors._
import v1.models.request.delete.DeleteRequestData

class DeleteValidatorFactorySpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino         = "AA123456A"
  private val validSubmissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val rawTaxYear        = "2023-24"

  class SetUp extends MockAppConfig {
    MockedAppConfig.minTaxYearCisDeductions.returns("2019")
    val validatorFactory = new DeleteValidatorFactory

    def validator(nino: String, submissionId: String, taxYear: Option[String]): Validator[DeleteRequestData] =
      validatorFactory.validator(nino, submissionId, taxYear)

  }

  "running a delete validation" should {

    "return no errors" when {
      "given a valid request" in new SetUp {
        val result: Either[ErrorWrapper, DeleteRequestData] = validator(validNino, validSubmissionId, Some(rawTaxYear)).validateAndWrapResult()
        result shouldBe Right(DeleteRequestData(Nino(validNino), SubmissionId(validSubmissionId), Some(TaxYear.fromMtd(rawTaxYear))))
      }
    }

    "return a single error" when {
      "given an invalid nino" in new SetUp {
        val result: Either[ErrorWrapper, DeleteRequestData] = validator("23456A", validSubmissionId, Some(rawTaxYear)).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given an invalid submission id" in new SetUp {
        val result: Either[ErrorWrapper, DeleteRequestData] = validator(validNino, "contractor1", Some(rawTaxYear)).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, SubmissionIdFormatError))
      }

      "given a pre-TYS taxYear param" in new SetUp {
        val result: Either[ErrorWrapper, DeleteRequestData] = validator(validNino, validSubmissionId, Some("2021-22")).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, InvalidTaxYearParameterError))
      }

      "given an invalid tax year" in new SetUp {
        val result: Either[ErrorWrapper, DeleteRequestData] = validator(validNino, validSubmissionId, Some("2023-25")).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return multiple errors" when {
      "given multiple wrong fields" in new SetUp {
        val result: Either[ErrorWrapper, DeleteRequestData] =
          validator("2sbt3456A", "idcontract123", Some("bad-tax-year-format")).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError, SubmissionIdFormatError))))
      }
    }
  }

}
