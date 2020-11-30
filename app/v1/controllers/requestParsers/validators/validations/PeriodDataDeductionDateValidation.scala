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

import play.api.libs.json.JsValue
import v1.models.errors._

object PeriodDataDeductionDateValidation {

  def validateDate(json: JsValue, fieldName: String, error: MtdError): List[MtdError] = {

    val periodData = (json \ "periodData").as[List[JsValue]]

    periodData.flatMap {
      period => DateValidation.validate(error)((period \ fieldName).as[String])
    }
  }

  def validateDateOrder(fromDate: String, toDate: String): List[MtdError] = {

    fromDate > toDate match {
      case true  => NoValidationErrors
      case false => List(RuleDeductionsDateRangeInvalidError)
    }
  }

  def validatePeriodInsideTaxYear(fromDate: String, toDate: String, deductionFromDate: String, deductionToDate: String): List[MtdError] = {

    (fromDate <= deductionFromDate, deductionFromDate < toDate, fromDate < deductionToDate, deductionToDate <= toDate) match {
      case (true, true, true, true)  => NoValidationErrors
      case _ => List(RuleDeductionsDateRangeInvalidError)
    }
  }




}
