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

import v1.models.errors.{FromDateFormatError, MtdError, RuleTaxYearNotSupportedError}

object MinTaxYearValidation {
  private val yearSize = 4

  // @param taxYear In format YYYY-MM-DD
  def validate(fromDate: String, minTaxYear: Int): List[MtdError] = {
    try {
      val fyTaxYear = Integer.parseInt(fromDate.take(yearSize))

      if (fyTaxYear >= minTaxYear) NoValidationErrors else List(RuleTaxYearNotSupportedError)
    } catch {
      case e: NumberFormatException => List(FromDateFormatError)
    }
  }
}
