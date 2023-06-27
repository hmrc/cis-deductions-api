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

package v2.controllers.requestParsers.validators

import api.models.errors._
import mocks.MockAppConfig
import support.UnitSpec
import v2.models.request.retrieve.RetrieveRawData

class RetrieveValidatorSpec extends UnitSpec {

  private val nino              = "AA123456A"
  private val invalidNino       = "GHFG197854"
  private val taxYearRaw        = "2019-20"
  private val invalidTaxYearRaw = "2019-2020"
  private val sourceRaw         = "all"
  private val invalidSource     = "All"

  class SetUp extends MockAppConfig {
    val validator = new RetrieveValidator(mockAppConfig)
    MockedAppConfig.minTaxYearCisDeductions.returns("2020")
  }

  "running validation" should {
    "return no errors" when {
      "the request is valid" in new SetUp {
        validator
          .validate(RetrieveRawData(nino, taxYearRaw, sourceRaw))
          .isEmpty shouldBe true
      }
    }

    "return errors" when {
      "invalid taxYear is passed in the request" in new SetUp {
        private val result = validator.validate(RetrieveRawData(nino, invalidTaxYearRaw, sourceRaw))
        result shouldBe List(TaxYearFormatError)
      }

      "invalid source data is passed in the request" in new SetUp {
        private val result = validator.validate(RetrieveRawData(nino, taxYearRaw, invalidSource))
        result shouldBe List(RuleSourceInvalidError)
      }

      "invalid nino, taxYear and source data is passed in the request" in new SetUp {
        private val result = validator.validate(RetrieveRawData(invalidNino, invalidTaxYearRaw, invalidSource))
        result shouldBe List(NinoFormatError, TaxYearFormatError, RuleSourceInvalidError)
      }

      "invalid taxYear range is passed in the request" in new SetUp {
        private val result = validator.validate(RetrieveRawData(nino, "2021-23", sourceRaw))
        result shouldBe List(RuleDateRangeInvalidError)
      }
    }
  }

}
