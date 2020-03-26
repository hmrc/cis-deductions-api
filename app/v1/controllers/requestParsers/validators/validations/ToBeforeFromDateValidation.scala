/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators.validations

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import v1.models.errors.MtdError

object ToBeforeFromDateValidation {

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def validate(fromDate: String, toDate: String, error: MtdError): List[MtdError] = {

    val fromDateEpochTime = LocalDate.parse(fromDate, dateTimeFormatter).toEpochDay
    val toDateEpochTime = LocalDate.parse (toDate, dateTimeFormatter).toEpochDay

    val diff = toDateEpochTime - fromDateEpochTime

    if(diff < 1 || diff > 366) List(error) else List()
  }
}
