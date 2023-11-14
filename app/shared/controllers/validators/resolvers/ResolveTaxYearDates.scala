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
import shared.models.errors.{MtdError, RuleDateRangeInvalidError}

import java.time.LocalDate

case class ResolveTaxYearDates(allowedNumberOfYearsBetweenDates: Int = 1) extends Resolver[(String, String), (String, String)] {
  private val fromDateFormat = "[0-9]{4}-04-06"
  private val toDateFormat   = "[0-9]{4}-04-05"

  private def resolveStartEndDate(startDate: LocalDate, endDate: LocalDate): Validated[Seq[MtdError], (String, String)] = {
    val validStart = startDate.toString.matches(fromDateFormat)
    val validEnd   = endDate.toString.matches(toDateFormat)
    (validStart, validEnd) match {
      case (true, true) => Valid((startDate.toString, endDate.toString))
      case _            => Invalid(Seq(RuleDateRangeInvalidError))
    }
  }

  private def validateYears(fromDate: LocalDate, toDate: LocalDate): Validated[Seq[MtdError], (String, String)] = {
    if ((toDate.getYear - fromDate.getYear) == allowedNumberOfYearsBetweenDates) {
      resolveStartEndDate(fromDate, toDate)
    } else {
      Invalid(Seq(RuleDateRangeInvalidError))
    }
  }

  def apply(dates: (String, String), unusedError: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], (String, String)] = {
    dates match {
      case (fromDate, toDate) =>
        val startDate = ResolveIsoDate(fromDate)
        val endDate   = ResolveIsoDate(toDate)
        (startDate, endDate) match {
          case (Valid(from), Valid(to)) =>
            validateYears(from, to)
          case _ => Invalid(List(RuleDateRangeInvalidError))
        }

    }

  }

}
