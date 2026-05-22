/*
 * Copyright 2026 HM Revenue & Customs
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

package v3.controllers.validators

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.*
import api.models.domain.{DateRange, TaxYear}
import api.models.errors.*
import cats.data.Validated
import cats.data.Validated.*
import cats.implicits.*
import config.CisDeductionsApiConfig
import models.errors.{ContractorNameFormatError, EmployerRefFormatError}
import play.api.libs.json.JsValue
import v3.controllers.validators.DeductionsValidator.*
import v3.models.request.create.{CreateBody, CreateRequestData}

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

  private val contractorNameRegex = "^.{1,105}$".r
  private val empRefFormat        = "[0-9]{3}/[^ ]{0,9}".r

  def validator(nino: String, body: JsValue): Validator[CreateRequestData] =
    new Validator[CreateRequestData] {

      def validate: Validated[Seq[MtdError], CreateRequestData] =
        (
          ResolveNino(nino),
          resolveJson(body)
        ).mapN(CreateRequestData.apply) andThen validateBusinessRules

      private def validateBusinessRules(parsed: CreateRequestData): Validated[Seq[MtdError], CreateRequestData] = {
        import parsed.body.periodData

        combine(
          validateDateRange(parsed.body.fromDate -> parsed.body.toDate),
          ResolveStringPattern(parsed.body.contractorName, contractorNameRegex, ContractorNameFormatError),
          ResolveStringPattern(parsed.body.employerRef, empRefFormat, EmployerRefFormatError),
          validatePeriodData(periodData)
        ).map(_ => parsed)
      }

      private val validateDateRange = {
        val taxYearOfDateRangeIsSupported =
          satisfiesMin(appConfig.minTaxYearCisDeductions, RuleTaxYearNotSupportedError)
            .contramap((dateRange: DateRange) => TaxYear.containing(dateRange.endDate))

        resolveDateRange.thenValidate(taxYearOfDateRangeIsSupported).thenValidate(checkDateRangeIsAFullTaxYear)
      }

    }

}
