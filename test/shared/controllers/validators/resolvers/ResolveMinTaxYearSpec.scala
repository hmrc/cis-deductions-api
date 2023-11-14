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

package shared.controllers.validators.resolvers

import cats.data.Validated.{Invalid, Valid}
import shared.UnitSpec
import shared.models.errors.{FromDateFormatError, RuleTaxYearNotSupportedError}

class ResolveMinTaxYearSpec extends UnitSpec {

  "ResolveMinTaxYear" should {
    "return no errors" when {
      "passed a valid date" in {
        val validDate = "2022-01-01"
        val minTaxYear = 2018
        val result = ResolveMinTaxYear.apply((validDate, minTaxYear), None, None)
        result shouldBe Valid(validDate)
      }
    }

    "return an error" when {
      "passed a date before min tax year" in {
        val invalidDate = "2017-01-01"
        val minTaxYear = 2018
        val result             = ResolveMinTaxYear.apply((invalidDate, minTaxYear), None, None)
        result shouldBe Invalid(List(RuleTaxYearNotSupportedError))
      }
      "passed an invalid date" in {
          val invalidDate = "2017x-01-01"
          val minTaxYear = 2018
          val result = ResolveMinTaxYear.apply((invalidDate, minTaxYear), None, None)
          result shouldBe Invalid(List(FromDateFormatError))
      }
    }
  }


}
