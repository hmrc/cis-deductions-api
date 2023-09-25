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

import api.controllers.resolvers._
import api.controllers.validators.Validator
import api.models.domain.Source
import api.models.errors._
import cats.data.Validated
import cats.data.Validated._
import cats.implicits._
import config.AppConfig
import v1.models.request.retrieve.RetrieveRequestData

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveValidatorFactory @Inject() (appConfig: AppConfig) {

  def validator(nino: String, fromDate: Option[String], toDate: Option[String], source: Option[String]): Validator[RetrieveRequestData] = {

    new Validator[RetrieveRequestData] {

      private def resolveDate(dateSupplied: Option[String], missingDate: MtdError, formatError: MtdError): Validated[Seq[MtdError], String] = {
        dateSupplied match {
          case Some(date) => ResolveDate(date, Some(formatError), None)
          case _          => Invalid(Seq(missingDate))
        }
      }

      private def resolveDateRange(startDate: LocalDate, endDate: LocalDate): Validated[Seq[MtdError], String] = {
        val taxYearRangeValidation = ResolveTaxYearDates().apply((startDate.toString, endDate.toString)).isValid
        val dateRangeValidation    = ResolveDateRange((startDate.toString, endDate.toString), None, None).isValid
        if (dateRangeValidation && taxYearRangeValidation) { Valid(endDate.toString) }
        else { Invalid(List(RuleDateRangeInvalidError)) }
      }

      private def resolveEndDate(from: Option[String], to: Option[String]): Validated[Seq[MtdError], String] = {
        val toDateValidation = resolveDate(to, RuleMissingToDateError, ToDateFormatError)
        (from, to) match {
          case (Some(startDate), Some(endDate)) =>
            ResolveIsoDate(endDate) match {
              case Valid(date) =>
                val end = date
                ResolveIsoDate(startDate) match {
                  case Valid(date) => resolveDateRange(date, end)
                  case Invalid(_)  => Invalid(List())
                }
              case Invalid(_) => toDateValidation
            }
          case _ => toDateValidation
        }
      }

      private def resolveSource(source: Option[String]): Validated[Seq[MtdError], Source] =
        source match {
          case Some(value) => ResolveSource(value)
          case _           => Valid(ResolveSource.defaultValue)
        }

      def validate: Validated[Seq[MtdError], RetrieveRequestData] = {
        (
          ResolveNino(nino),
          resolveDate(fromDate, RuleMissingFromDateError, FromDateFormatError),
          resolveEndDate(fromDate, toDate),
          resolveSource(source)
        ).mapN(RetrieveRequestData)
      }
    }
  }

}
