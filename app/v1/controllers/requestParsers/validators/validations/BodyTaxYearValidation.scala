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

import v1.models.errors.MtdError

object BodyTaxYearValidation {

  def validate(date: String, fieldName: String, error: MtdError): List[MtdError] = {
    fieldName match {
      case "fromDate" => validateFromDate(error)(date)
      case "toDate" => validateToDate(error)(date)
    }
  }

  val fromDateFormat = "[0-9][0-9][0-9][0-9]-[0][4]-[0][6]"
  val toDateFormat = "[0-9][0-9][0-9][0-9]-[0][4]-[0][5]"

  def validateFromDate(error: MtdError)(date: String): List[MtdError] = {
    if (date.matches(fromDateFormat)) NoValidationErrors else List(error)
  }

  def validateToDate(error: MtdError)(date: String): List[MtdError] = {
    if (date.matches(toDateFormat)) NoValidationErrors else List(error)
  }
}
