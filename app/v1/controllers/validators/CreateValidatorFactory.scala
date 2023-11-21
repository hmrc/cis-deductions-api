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
import config.AppConfig
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers._
import shared.models.domain.{DateRange, TaxYear}
import shared.models.errors._
import v1.controllers.validators.resolvers.ResolveEmployeeRef
import v1.models.errors.CisDeductionsApiCommonErrors.{DeductionFromDateFormatError, DeductionToDateFormatError}
import v1.models.request.amend.PeriodDetails
import v1.models.request.create.{CreateBody, CreateRequestData}

import javax.inject.{Inject, Singleton}

@Singleton
class CreateValidatorFactory @Inject() (appConfig: AppConfig) {

  private val minYear = 1900
  private val maxYear = 2100

  // The CIS-Deductions config has an int e.g. 2019 which is specified as "2019-20";
  // however TaxYear.fromDownstreamInt puts the year last i.e. "2018-19".
  // So the adjustment here is made:
  private lazy val minTaxYearCisDeductions = appConfig.minTaxYearCisDeductions.toInt + 1
  private lazy val resolveTaxYearMinimum   = ResolveTaxYearMinimum(TaxYear.fromDownstreamInt(minTaxYearCisDeductions))

  private val resolveAmount = ResolveParsedNumber()
  private val resolveJson   = new ResolveJsonObject[CreateBody]()

  private val resolveDateRange = ResolveDateRange(
    startDateFormatError = FromDateFormatError,
    endDateFormatError = ToDateFormatError,
    endBeforeStartDateError = RuleDateRangeInvalidError
  ).withYearsLimitedTo(minYear, maxYear)

  private val resolveDeductionDateRange = ResolveDateRange(
    startDateFormatError = DeductionFromDateFormatError,
    endDateFormatError = DeductionToDateFormatError
  ).withYearsLimitedTo(minYear, maxYear)

  def validator(nino: String, body: JsValue): Validator[CreateRequestData] =
    new Validator[CreateRequestData] {

      def validate: Validated[Seq[MtdError], CreateRequestData] =
        (
          ResolveNino(nino),
          resolveJson(body)
        ).mapN(CreateRequestData) andThen validateBusinessRules

      private def validateBusinessRules(parsed: CreateRequestData): Validated[Seq[MtdError], CreateRequestData] = {
        import parsed.body.periodData

        if (periodData.isEmpty) {
          Invalid(List(RuleIncorrectOrEmptyBodyError))
        } else {
          val validatedPeriodData =
            periodData
              .traverse(periodDetail => validatePeriodDetails(periodDetail))

          combine(
            resolveDateRange(parsed.body.fromDate -> parsed.body.toDate) andThen validateRangeAsTaxYear,
            ResolveEmployeeRef(parsed.body.employerRef),
            validatedPeriodData
          ).map(_ => parsed)

        }
      }

      private def validateRangeAsTaxYear(dateRange: DateRange): Validated[Seq[MtdError], Unit] =
        resolveTaxYearMinimum(dateRange.asTaxYearMtdString) match {
          case Invalid(List(RuleTaxYearNotSupportedError)) => Invalid(List(RuleTaxYearNotSupportedError))
          case Invalid(_)                                  => Invalid(List(RuleDateRangeInvalidError))
          case Valid(_)                                    => Valid(())
        }

      private def validateAmount(error: MtdError, maybeValue: Option[BigDecimal]): Validated[Seq[MtdError], Unit] =
        maybeValue.map(validateAmount(error, _)).getOrElse(Valid(()))

      private def validateAmount(error: MtdError, value: BigDecimal): Validated[Seq[MtdError], Unit] =
        resolveAmount.resolver(error)(value).map(_ => ())

      private def validatePeriodDetails(details: PeriodDetails): Validated[Seq[MtdError], Unit] =
        combine(
          validateAmount(RuleDeductionAmountError, details.deductionAmount),
          validateAmount(RuleCostOfMaterialsError, details.costOfMaterials),
          validateAmount(RuleGrossAmountError, details.grossAmountPaid),
          resolveDeductionDateRange(details.deductionFromDate -> details.deductionToDate)
        )

    }

}
