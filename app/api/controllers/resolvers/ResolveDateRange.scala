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

package api.controllers.resolvers

import api.models.errors.{EndDateFormatError, MtdError, RuleEndBeforeStartDateError, StartDateFormatError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._

import java.time.LocalDate

case class ResolveDateRange(startDate: LocalDate, endDate: LocalDate)

object ResolveDateRange extends Resolver[(String, String), ResolveDateRange] {

  def apply(value: (String, String), notUsedError: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], ResolveDateRange] = {
    val (startDate, endDate) = value
    (
      ResolveIsoDate(startDate, StartDateFormatError),
      ResolveIsoDate(endDate, EndDateFormatError)
    ).mapN(resolveDateRange(_, _, notUsedError.getOrElse(RuleEndBeforeStartDateError))).andThen(identity)
  }

  private def resolveDateRange(parsedStartDate: LocalDate, parsedEndDate: LocalDate, error: MtdError): Validated[Seq[MtdError], ResolveDateRange] = {
    val startDateEpochTime = parsedStartDate.toEpochDay
    val endDateEpochTime   = parsedEndDate.toEpochDay
    if ((endDateEpochTime - startDateEpochTime) <= 0) {
      Invalid(List(error))
    } else {
      Valid(ResolveDateRange(parsedStartDate, parsedEndDate))
    }
  }

}
