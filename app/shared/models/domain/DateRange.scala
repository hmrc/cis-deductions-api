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

import java.time.LocalDate

case class DateRange(startDate: LocalDate, endDate: LocalDate) {

  /** The startDate and endDate must form a valid tax year; validating/resolving should be done separately.
    * @return
    *   e.g. for 2020-05-03 -> 2021-06-02, "2020-21"
    */
  def asTaxYearMtdString: String = {
    val start = startDate.toString.take(4)
    val end   = endDate.toString.take(4).takeRight(2)
    s"$start-$end"
  }

}

object DateRange {

  def apply(range: (LocalDate, LocalDate)): DateRange = {
    val (startDate, endDate) = range
    new DateRange(startDate, endDate)
  }

}
