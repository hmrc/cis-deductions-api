/*
 * Copyright 2025 HM Revenue & Customs
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

package v2.controllers.validators

import cats.data.Validated
import cats.data.Validated.*
import cats.implicits.*
import config.CisDeductionsApiConfig
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.*
import shared.models.domain.{DateRange, TaxYear}
import shared.models.errors.*
import v2.controllers.validators.DeductionsValidator.*
import v2.controllers.validators.resolvers.ResolveEmployeeRef
import v2.models.request.create.CreateBody
import v2.models.request.create.CreateRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class CreateValidatorFactory @Inject() (appConfig: CisDeductionsApiConfig) {

  private val resolveJson = new ResolveJsonObject[CreateBody]()

  private val resolveDateRange = ResolveDateRange(
    startDateFormatError = FromDateFormatError,
    endDateFormatError = ToDateFormatError,
    endBeforeStartDateError = RuleDateRangeInvalidError
  )
    .withYearsLimitedTo(minYear, maxYear)

  def validator(nino: String, body: JsValue): Validator[CreateRequestData] =
    new Validator[CreateRequestData] {

      def validate: Validated[Seq[MtdError], CreateRequestData] =
        (
          ResolveNino(nino),
          resolveJson(body)
        ).mapN(CreateRequestData) andThen validateBusinessRules

      private def validateBusinessRules(parsed: CreateRequestData): Validated[Seq[MtdError], CreateRequestData] = {
        import parsed.body.periodData

        combine(
          validateDateRange(parsed.body.fromDate -> parsed.body.toDate),
          ResolveEmployeeRef(parsed.body.employerRef),
          validatePeriodData(periodData)
        ).map(_ => parsed)
      }

      private val validateDateRange = {
        val taxYearOfDateRangeIsSupported =
          satisfiesMin(appConfig.minTaxYearCisDeductions, RuleTaxYearNotSupportedError)
            .contramap((dateRange: DateRange) => TaxYear.containing(dateRange.endDate))

        resolveDateRange thenValidate taxYearOfDateRangeIsSupported thenValidate checkDateRangeIsAFullTaxYear
      }

    }

}
