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

import api.models.domain.TaxYear
import api.models.errors.{InvalidTaxYearParameterError, MtdError}
import config.FixedConfig

object TaxYearTysParameterValidation extends FixedConfig {

  def validate(maybeTaxYear: Option[String]): List[MtdError] = maybeTaxYear.map(validate).getOrElse(Nil)

  def validate(taxYear: String): List[MtdError] =
    try {
      val year = TaxYear.fromMtd(taxYear).year
      if (year >= TaxYear.minimumTysTaxYear) NoValidationErrors else List(InvalidTaxYearParameterError)
    } catch {
      case _: NumberFormatException => NoValidationErrors // has a separate date-format validation
    }

}
