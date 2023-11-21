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

package v1.models.request.retrieve

import shared.models.domain.{DateRange, Nino, Source, TaxYear}

/** @param fromDate
  *   period start in extended ISO-8601 format (e.g. 2020-04-01)
  * @param toDate
  *   period end e.g. 2021-09-01
  */
case class RetrieveRequestData(nino: Nino, dateRange: DateRange, source: Source) {
  val fromDate: String = dateRange.startDate.toString
  val toDate: String   = dateRange.endDate.toString
  val taxYear: TaxYear = TaxYear.fromIso(toDate)
}
