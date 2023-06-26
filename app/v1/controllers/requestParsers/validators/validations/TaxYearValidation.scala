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

package v1.controllers.requestParsers.validators.validations

import api.controllers.requestParsers.validators.validations.NoValidationErrors
import api.models.errors.{MtdError, RuleTaxYearRangeExceededError, TaxYearFormatError}

object TaxYearValidation {

  val taxYearFormat = "20[1-9][0-9]\\-[1-9][0-9]"

  def validate(maybeTaxYear: Option[String]): List[MtdError] = maybeTaxYear.map(validate).getOrElse(Nil)

  def validate(taxYear: String): List[MtdError] = {
    if (taxYear.matches(taxYearFormat)) {

      val startTaxYearStart: Int = 2
      val startTaxYearEnd: Int   = 4

      val endTaxYearStart: Int = 5
      val endTaxYearEnd: Int   = 7

      val start = taxYear.substring(startTaxYearStart, startTaxYearEnd).toInt
      val end   = taxYear.substring(endTaxYearStart, endTaxYearEnd).toInt

      if (end - start == 1) {
        NoValidationErrors
      } else {
        List(RuleTaxYearRangeExceededError)
      }
    } else {
      List(TaxYearFormatError)
    }
  }

}
