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

package shared.controllers.validators.resolvers

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import shared.models.errors.{FromDateFormatError, MtdError, RuleTaxYearNotSupportedError}

object ResolveMinTaxYear extends Resolver[(String, Int), String] {

  def apply(value: (String, Int), error: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], String] = {
    val yearSize               = 4
    val (fromDate, minTaxYear) = value

    ResolveDate(fromDate, Some(FromDateFormatError), None) match {
      case Invalid(error) => Invalid(error)
      case Valid(_) =>
        try {
          val taxYear = Integer.parseInt(fromDate.take(yearSize))
          if (taxYear >= minTaxYear) {
            Valid(fromDate)
          } else {
            Invalid(List(requireError(Some(RuleTaxYearNotSupportedError), path)))
          }
        } catch {
          case _: NumberFormatException => Invalid(List(requireError(Some(FromDateFormatError), path)))
        }
    }
  }

}
