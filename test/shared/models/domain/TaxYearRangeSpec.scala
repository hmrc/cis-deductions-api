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

package shared.models.domain

import shared.UnitSpec

import java.time.LocalDate

class TaxYearRangeSpec extends UnitSpec {

  "TaxYearRange" should {
    "return the correct fromStartDate and toStart when called" in {
      val taxYearRange = TaxYearRange.fromMtd("2023-24")
      taxYearRange.from shouldBe TaxYear("2024")
      taxYearRange.to shouldBe TaxYear("2024")
    }
    "return a one year range when fromMtd is called " in {
      val taxYear = "2023-24"
      TaxYearRange.fromMtd(taxYear) shouldBe TaxYearRange(TaxYear("2024"))
    }

    "return the correct tax year range when todayMinus 5 is called" in {

      val fiveYears = 5
      val result  = TaxYearRange.todayMinus(fiveYears)
      val currentTaxYear = TaxYear.currentTaxYear()
      val taxYearMinusFive  = TaxYear.fromIso(LocalDate.now().minusYears(fiveYears).toString())

      result shouldBe TaxYearRange(taxYearMinusFive, currentTaxYear)
    }

  }

}
