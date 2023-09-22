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

package v2.controllers.resolvers

import api.controllers.resolvers.Resolver
import api.models.domain.TaxYear
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

trait ResolvingTaxYear extends Resolver[String, TaxYear] {

  private val taxYearFormat = "20[1-9][0-9]-[1-9][0-9]".r

  protected def resolve(value: String, error: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], TaxYear] = {
    if (taxYearFormat.matches(value)) {
      val startTaxYearStart: Int = 2
      val startTaxYearEnd: Int   = 4

      val endTaxYearStart: Int = 5
      val endTaxYearEnd: Int   = 7

      val start = value.substring(startTaxYearStart, startTaxYearEnd).toInt
      val end   = value.substring(endTaxYearStart, endTaxYearEnd).toInt

      if (end - start == 1) {
        Valid(TaxYear.fromMtd(value))
      } else {
        Invalid(List(withError(error, RuleDateRangeInvalidError, path)))
      }

    } else {
      Invalid(List(withError(error, TaxYearFormatError, path)))
    }
  }

}

object ResolveTaxYear extends ResolvingTaxYear {

  def apply(value: String, error: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], TaxYear] =
    resolve(value, error, path)

  def apply(minimumTaxYear: Int, value: String, error: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], TaxYear] =
    resolve(value, error, path)
      .andThen { taxYear =>
        if (taxYear.year < minimumTaxYear) {
          Invalid(List(RuleTaxYearNotSupportedError))
        } else {
          Valid(taxYear)
        }
      }

}

object ResolveTysTaxYear extends ResolvingTaxYear {

  def apply(value: String, error: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], TaxYear] =
    resolve(value, error, path)
      .andThen { taxYear =>
        if (taxYear.year < TaxYear.tysTaxYear) {
          Invalid(List(InvalidTaxYearParameterError) ++ error)
        } else {
          Valid(taxYear)
        }
      }

}
