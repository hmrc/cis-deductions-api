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

package v2.controllers.validators

import models.domain.CisSource
import models.errors.RuleSourceInvalidError
import shared.config.MockAppConfig
import shared.controllers.validators.Validator
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.utils.UnitSpec
import v2.models.request.retrieve.RetrieveRequestData

class RetrieveValidatorFactorySpec extends UnitSpec {
  private implicit val correlationId: String = "1234"

  private val nino              = "AA123456A"
  private val invalidNino       = "GHFG197854"
  private val taxYearRaw        = "2019-20"
  private val invalidTaxYearRaw = "2019-2020"
  private val sourceRaw         = CisSource.`all`
  private val invalidSource     = "All"

  class SetUp extends MockAppConfig {
    val validatorFactory = new RetrieveValidatorFactory()

    def validator(nino: String, taxYear: String, source: String): Validator[RetrieveRequestData] =
      validatorFactory.validator(nino, taxYear, source)

  }

  "running validation" should {
    "return no errors" when {
      "the request is valid" in new SetUp {
        val result: Either[ErrorWrapper, RetrieveRequestData] =
          validator(nino, taxYearRaw, sourceRaw.toString)
            .validateAndWrapResult()
        result shouldBe Right(RetrieveRequestData(Nino(nino), TaxYear.fromMtd(taxYearRaw), sourceRaw))
      }
    }

    "return errors" when {
      "invalid taxYear is passed in the request" in new SetUp {
        val result: Either[ErrorWrapper, RetrieveRequestData] = validator(nino, invalidTaxYearRaw, sourceRaw.toString).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "invalid source data is passed in the request" in new SetUp {
        val result: Either[ErrorWrapper, RetrieveRequestData] = validator(nino, taxYearRaw, invalidSource).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleSourceInvalidError))
      }

      "invalid nino, taxYear and source data is passed in the request" in new SetUp {
        val result: Either[ErrorWrapper, RetrieveRequestData] = validator(invalidNino, invalidTaxYearRaw, invalidSource).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError, RuleSourceInvalidError))))
      }

      "invalid taxYear range is passed in the request" in new SetUp {
        val result: Either[ErrorWrapper, RetrieveRequestData] = validator(nino, "2021-23", sourceRaw.toString).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }
  }

}
