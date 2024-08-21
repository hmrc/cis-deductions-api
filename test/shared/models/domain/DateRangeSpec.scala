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

import shared.utils.UnitSpec

import java.time.LocalDate

class DateRangeSpec extends UnitSpec {

  private val startDate = LocalDate.parse("2023-06-01")
  private val endDate   = LocalDate.parse("2024-05-07")

  "apply(LocalDate,LocalDate)" should {
    "return a DateRange" in {
      DateRange(startDate -> endDate) shouldBe DateRange(startDate, endDate)
    }
  }

}
