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
import v1.models.request.retrieve.RetrieveRequestData
import v2.controllers.validators.resolvers.ResolveSource

import javax.inject.Singleton

@Singleton
class RetrieveValidatorFactory {

  def validator(nino: String, fromDate: Option[String], toDate: Option[String], source: Option[String]): Validator[RetrieveRequestData] = {
    new Validator[RetrieveRequestData] {

      private val resolveTaxYear  = ResolveTaxYear.resolver
      private val resolveFromDate = ResolveIsoDate(FromDateFormatError)
      private val resolveToDate   = ResolveIsoDate(ToDateFormatError)

      def validate: Validated[Seq[MtdError], RetrieveRequestData] = {
        val resolvedDateRange =
          parseDateRange(fromDate, toDate) andThen validateStartAndEndDates andThen validateRangeAsTaxYear

        (
          ResolveNino(nino),
          resolvedDateRange,
          resolveSource(source)
        ).mapN(RetrieveRequestData)
      }

      private def parseDateRange(maybeFrom: Option[String], maybeTo: Option[String]): Validated[Seq[MtdError], DateRange] = {
        (maybeFrom, maybeTo) match {
          case (None, Some(_)) =>
            Invalid(List(RuleMissingFromDateError))

          case (Some(_), None) =>
            Invalid(List(RuleMissingToDateError))

          case (None, None) =>
            Invalid(List(RuleMissingFromDateError, RuleMissingToDateError))

          case (Some(fromDateStr), Some(toDateStr)) =>
            (
              resolveFromDate(fromDateStr),
              resolveToDate(toDateStr)
            ).mapN((parsedFromDate, parsedToDate) => DateRange(parsedFromDate, parsedToDate))
        }
      }

      private def validateStartAndEndDates(dateRange: DateRange): Validated[Seq[MtdError], DateRange] = {
        import dateRange._
        if (startDate.getMonthValue == 4 &&
          startDate.getDayOfMonth == 6 &&
          endDate.getMonthValue == 4 &&
          endDate.getDayOfMonth == 5) Valid(dateRange)
        else
          Invalid(List(RuleDateRangeInvalidError))
      }

      private def validateRangeAsTaxYear(dateRange: DateRange): Validated[Seq[MtdError], DateRange] =
        resolveTaxYear(dateRange.asTaxYearMtdString) match {
          case Invalid(List(RuleTaxYearNotSupportedError)) => Invalid(List(RuleTaxYearNotSupportedError))
          case Invalid(_)                                  => Invalid(List(RuleDateRangeInvalidError))
          case Valid(_)                                    => Valid(dateRange)
        }

      private def resolveSource(source: Option[String]): Validated[Seq[MtdError], Source] =
        source match {
          case Some(value) => ResolveSource(value)
          case _           => Valid(Source.`all`)
        }
    }
  }

}
