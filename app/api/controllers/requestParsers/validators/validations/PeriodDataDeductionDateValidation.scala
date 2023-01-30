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

package api.controllers.requestParsers.validators.validations

import api.controllers.requestParsers.validators.validations.validations.NoValidationErrors
import api.models.domain.TaxYear
import api.models.errors.{MtdError, RuleDeductionsDateRangeInvalidError, RuleUnalignedDeductionsPeriodError}
import play.api.libs.json.JsValue
import v1.models.request.amend.{AmendBody, AmendRawData}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PeriodDataDeductionDateValidation {

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def validateDate(json: JsValue, fieldName: String, error: MtdError): List[MtdError] = {

    val periodData = (json \ "periodData").as[List[JsValue]]

    periodData.flatMap { period =>
      DateValidation.validate(error)((period \ fieldName).as[String])
    }
  }

  def validateDateOrder(fromDate: String, toDate: String): List[MtdError] = {

    val dateFormatErrs = validateFromDate(fromDate) ++
      validateToDate(toDate)
    if (dateFormatErrs.isEmpty) validateMonths(fromDate, toDate) else dateFormatErrs
  }

  val fromDateFormat = "[0-9]{4}-[0-9][0-9]-06"
  val toDateFormat   = "[0-9]{4}-[0-9][0-9]-05"

  private def validateFromDate(fromDate: String): List[MtdError] = {
    if (fromDate.matches(fromDateFormat)) NoValidationErrors else List(RuleDeductionsDateRangeInvalidError)
  }

  private def validateToDate(toDate: String): List[MtdError] = {
    if (toDate.matches(toDateFormat)) NoValidationErrors else List(RuleDeductionsDateRangeInvalidError)
  }

  private def validateMonths(fromDate: String, toDate: String): List[MtdError] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val startDate = LocalDate.parse(fromDate, formatter)
    val endDate   = LocalDate.parse(toDate, formatter)
    val diff      = endDate.getMonth.getValue - startDate.getMonth.getValue
    (diff == 1 && endDate.getYear == startDate.getYear, diff == -11 && endDate.getYear - startDate.getYear == 1) match {
      case (false, false) => List(RuleDeductionsDateRangeInvalidError)
      case _              => NoValidationErrors
    }
  }

  def validateTaxYearForMultiplePeriods(data: AmendRawData): List[MtdError] = {

    val requestBody                = data.body.as[AmendBody]
    val distinctTaxYears: Seq[Int] = requestBody.periodData.collect(_.deductionToDate).map(TaxYear.fromIso(_).year).distinct

    if (distinctTaxYears.length == 1) NoValidationErrors else List(RuleUnalignedDeductionsPeriodError)
  }

  def validatePeriodInsideTaxYear(fromDate: String, toDate: String, deductionFromDate: String, deductionToDate: String): List[MtdError] = {

    val formatter          = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val taxYearStartDate   = LocalDate.parse(fromDate, formatter)
    val DeductionStartDate = LocalDate.parse(deductionFromDate, formatter)
    val taxYearEndDate     = LocalDate.parse(toDate, formatter)
    val DeductionEndDate   = LocalDate.parse(deductionToDate, formatter)

    (DeductionStartDate.isBefore(taxYearStartDate), DeductionEndDate.isAfter(taxYearEndDate)) match {
      case (false, false) => NoValidationErrors
      case _              => List(RuleUnalignedDeductionsPeriodError)
    }
  }

}