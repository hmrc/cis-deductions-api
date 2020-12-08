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

import play.api.libs.json.JsValue
import v1.models.errors._

object PeriodDataDeductionDateValidation {

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def validateDate(json: JsValue, fieldName: String, error: MtdError): List[MtdError] = {

    val periodData = (json \ "periodData").as[List[JsValue]]

    periodData.flatMap {
      period => DateValidation.validate(error)((period \ fieldName).as[String])
    }
  }

  def validateDateOrder(fromDate: String, toDate: String): List[MtdError] = {

    val dateFormatErrs = validateFromDate(fromDate) ++
      validateToDate(toDate)
    if (dateFormatErrs.isEmpty) validateMonths(fromDate, toDate) else dateFormatErrs
  }

    val fromDateFormat = "[0-9]{4}-[0-9][0-9]-06"
    val toDateFormat = "[0-9]{4}-[0-9][0-9]-05"

    private def validateFromDate(fromDate: String): List[MtdError] = {
      if (fromDate.matches(fromDateFormat)) NoValidationErrors else List(RuleDeductionsDateRangeInvalidError)
    }

    private def validateToDate(toDate: String): List[MtdError] = {
      if (toDate.matches(toDateFormat)) NoValidationErrors else List(RuleDeductionsDateRangeInvalidError)
    }

    private def validateMonths(fromDate: String, toDate: String): List[MtdError] = {
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val startDate = LocalDate.parse(fromDate, formatter)
      val endDate = LocalDate.parse(toDate, formatter)
      val diff = endDate.getMonth.getValue - startDate.getMonth.getValue
      (diff == 1 && endDate.getYear == startDate.getYear, diff == -11 && endDate.getYear - startDate.getYear == 1) match {
        case (false, false) => List(RuleDeductionsDateRangeInvalidError)
        case _            => NoValidationErrors
      }
    }



  def validatePeriodInsideTaxYear(fromDate: String, toDate: String, deductionFromDate: String, deductionToDate: String): List[MtdError] = {

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val taxYearStartDate = LocalDate.parse(fromDate, formatter)
    val DeductionStartDate = LocalDate.parse(deductionFromDate, formatter)
    val taxYearEndDate = LocalDate.parse(toDate, formatter)
    val DeductionEndDate = LocalDate.parse(deductionToDate, formatter)

    (DeductionStartDate.isBefore(taxYearStartDate),
      DeductionEndDate.isAfter(taxYearEndDate)) match {
      case (false, false)  => NoValidationErrors
      case _ => List(RuleUnalignedDeductionsPeriodError)
    }
  }
}
