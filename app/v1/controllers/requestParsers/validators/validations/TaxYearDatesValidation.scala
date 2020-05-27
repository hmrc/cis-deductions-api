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
import v1.models.errors.{MtdError, RuleDateRangeInvalidError, RuleFromDateError, RuleToDateError}

object TaxYearDatesValidation {

  def validate(fromDate: String, toDate: String, numYears: Option[Int]): List[MtdError] = {
    val dateFormatErrs = validateFromDate(fromDate) ++
    validateToDate(toDate)
    if(numYears.isDefined & dateFormatErrs.isEmpty) validateYears(numYears.get, fromDate, toDate) else dateFormatErrs
  }

  val fromDateFormat = "[0-9]{4}-04-06"
  val toDateFormat = "[0-9]{4}-04-05"

  private def validateFromDate(date: String): List[MtdError] = {
    if (date.matches(fromDateFormat)) NoValidationErrors else List(RuleDateRangeInvalidError)
  }

  private def validateToDate(date: String): List[MtdError] = {
    if (date.matches(toDateFormat)) NoValidationErrors else List(RuleDateRangeInvalidError)
  }

  private def validateYears(numYears: Int, fromDate: String, toDate: String): List[MtdError] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val startDate = LocalDate.parse(fromDate, formatter)
    val endDate = LocalDate.parse(toDate, formatter)
    val diff = endDate.getYear - startDate.getYear
    if (diff != numYears) List(RuleDateRangeInvalidError) else List()
  }
}
