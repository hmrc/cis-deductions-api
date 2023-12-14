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

package v1.controllers.validators

import cats.data.Validated
import cats.data.Validated._
import cats.implicits._
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers._
import shared.models.domain.{DateRange, Source}
import shared.models.errors._
import v1.controllers.validators.DeductionsValidator._
import v1.models.request.retrieve.RetrieveRequestData
import v2.controllers.validators.resolvers.ResolveSource

import javax.inject.Singleton

@Singleton
class RetrieveValidatorFactory {

  def validator(nino: String, fromDate: Option[String], toDate: Option[String], source: Option[String]): Validator[RetrieveRequestData] = {
    new Validator[RetrieveRequestData] {

      def validate: Validated[Seq[MtdError], RetrieveRequestData] = {
        (
          ResolveNino(nino),
          resolveDateRange((fromDate, toDate)),
          resolveSource(source)
        ).mapN(RetrieveRequestData)
      }

    }
  }

  private val resolveSource = ResolveSource.resolver.resolveOptionallyWithDefault(Source.`all`)

  private val resolveDateRange = {
    val resolveFromDate = ResolveIsoDate(FromDateFormatError)
    val resolveToDate   = ResolveIsoDate(ToDateFormatError)

    val parseDateRange: Resolver[(Option[String], Option[String]), DateRange] = {
      case (None, Some(_)) => Invalid(List(RuleMissingFromDateError))
      case (Some(_), None) => Invalid(List(RuleMissingToDateError))
      case (None, None)    => Invalid(List(RuleMissingFromDateError, RuleMissingToDateError))
      case (Some(fromDateStr), Some(toDateStr)) =>
        (
          resolveFromDate(fromDateStr),
          resolveToDate(toDateStr)
        ).mapN(DateRange(_, _))
    }

    parseDateRange thenValidate checkDateRangeIsAFullTaxYear
  }

}
