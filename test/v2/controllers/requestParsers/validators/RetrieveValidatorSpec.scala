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

  private val nino        = "AA123456A"
  private val invalidNino = "GHFG197854"
  private val taxYearRaw  = "2019-20"

  class SetUp extends MockAppConfig {
    val validator = new RetrieveValidator(mockAppConfig)
    MockedAppConfig.minTaxYearCisDeductions.returns("2020")
  }

  "running validation" should {
    "return no errors" when {
      "all query parameters are passed in the request" in new SetUp {
        validator
          .validate(RetrieveRawData(nino, taxYearRaw, "all"))
          .isEmpty shouldBe true
      }
    }

    "return errors" when {
      "invalid nino and source data is passed in the request" in new SetUp {
        private val result = validator.validate(RetrieveRawData(invalidNino, taxYearRaw, "All"))
        result shouldBe List(NinoFormatError, RuleSourceError)
      }
    }
  }

}
