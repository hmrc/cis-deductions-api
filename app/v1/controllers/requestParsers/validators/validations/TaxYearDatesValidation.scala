/*
 * Copyright 2022 HM Revenue & Customs
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

import v1.models.errors.{MtdError, RuleDateRangeInvalidError, RuleTaxYearNotEndedError}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object TaxYearDatesValidation {

  def validate(fromDate: String, toDate: String, allowedNumberOfYearsBetweenDates: Int, validateTaxYearEndedFlag: Boolean): List[MtdError] = {
    lazy val taxYearEndedErrors = if (validateTaxYearEndedFlag) {
      validateTaxYearEnded(fromDate, toDate)
    } else {
      NoValidationErrors
    }
    // Will return an error if one or both of these is on the incorrect day of the year
    val fromAndToDateErrors = validateFromDate(fromDate) ++ validateToDate(toDate)

    fromAndToDateErrors match {
      case Nil =>
        validateYears(allowedNumberOfYearsBetweenDates, fromDate, toDate) match {
          case Nil  => taxYearEndedErrors
          case errs => errs
        }
      case errs => errs
    }
  }

  private def validateTaxYearEnded(fromDate: String, toDate: String): List[MtdError] = {
    val currentDate = LocalDate.now()
    val formatter   = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val startDate   = LocalDate.parse(fromDate, formatter)
    val endDate     = LocalDate.parse(toDate, formatter)
    (startDate.isBefore(currentDate), endDate.isBefore(currentDate)) match {
      case (false, _)    => List(RuleDateRangeInvalidError)
      case (true, false) => List(RuleTaxYearNotEndedError)
      case _             => NoValidationErrors
    }
  }

  val fromDateFormat = "[0-9]{4}-04-06"
  val toDateFormat   = "[0-9]{4}-04-05"

  private def validateFromDate(date: String): List[MtdError] = {
    if (date.matches(fromDateFormat)) NoValidationErrors else List(RuleDateRangeInvalidError)
  }

  private def validateToDate(date: String): List[MtdError] = {
    if (date.matches(toDateFormat)) NoValidationErrors else List(RuleDateRangeInvalidError)
  }

  private def validateYears(allowedNumberOfYearsBetweenDates: Int, fromDate: String, toDate: String): List[MtdError] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val startDate = LocalDate.parse(fromDate, formatter)
    val endDate   = LocalDate.parse(toDate, formatter)
    val diff      = endDate.getYear - startDate.getYear
    if (diff != allowedNumberOfYearsBetweenDates) List(RuleDateRangeInvalidError) else NoValidationErrors
  }

}
