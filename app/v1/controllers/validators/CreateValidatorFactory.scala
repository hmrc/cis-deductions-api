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
import shared.controllers.validators.resolvers._
import shared.controllers.validators.{RulesValidator, Validator}
import shared.models.errors._
import v1.controllers.validators.resolvers.ResolveEmployeeRef
import v1.models.request.amend.PeriodDetails
import v1.models.request.create.{CreateBody, CreateRequestData}

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

@Singleton
class CreateValidatorFactory @Inject() (appConfig: AppConfig) extends RulesValidator[CreateRequestData] {

  private val minYear = 1900
  private val maxYear = 2100

  private val resolveAmount       = ResolveParsedNumber()
  private val resolveJson         = new ResolveJsonObject[CreateBody]()
  private val resolveTaxYearDates = ResolveTaxYearDates()

  private val resolveDateRange = ResolveDateRange(
//    RuleDateRangeInvalidError,
//    RuleDateRangeInvalidError,
//    RuleDateRangeInvalidError
  ).withYearsLimitedTo(minYear, maxYear)

  def validator(nino: String, body: JsValue): Validator[CreateRequestData] =
    new Validator[CreateRequestData] {

      def validate: Validated[Seq[MtdError], CreateRequestData] =
        (
          ResolveNino(nino),
          resolveJson(body, Some(RuleIncorrectOrEmptyBodyError), None)
        ).mapN(CreateRequestData) andThen validateBusinessRules

    }

  private def validateAmount(error: MtdError, maybeValue: Option[BigDecimal]): Validated[Seq[MtdError], Unit] =
    maybeValue.map(validateAmount(error, _)).getOrElse(Valid(()))

  private def validateAmount(error: MtdError, value: BigDecimal): Validated[Seq[MtdError], Unit] =
    resolveAmount.resolver(error)(value).map(_ => ())

  private def resolveRange(startDate: LocalDate, endDate: LocalDate): Validated[Seq[MtdError], String] = {
    val dateRangeValid    = resolveDateRange(startDate.toString -> endDate.toString).isValid
    val taxYearRangeValid = resolveTaxYearDates(startDate.toString -> endDate.toString).isValid

    if (dateRangeValid && taxYearRangeValid)
      Valid(endDate.toString)
    else
      Invalid(List(RuleDateRangeInvalidError))
  }

  private def resolveEndDate(startDate: String, endDate: String, formatError: MtdError): Validated[Seq[MtdError], Unit] = {
    ResolveMinTaxYear((startDate, appConfig.minTaxYearCisDeductions.toInt), Some(RuleTaxYearNotSupportedError), None) match {
      case Valid(_) =>
        resolveToDate(startDate, endDate, formatError, includeRangeValidation = true).map(_ => ())
      case Invalid(error) => Invalid(error)
    }
  }

  private def resolveToDate(startDate: String,
                            endDate: String,
                            formatError: MtdError,
                            includeRangeValidation: Boolean = false): Validated[Seq[MtdError], Unit] = {

    val validatedEndDate = ResolveIsoDate(endDate, formatError)
    val toDateValidation = ResolveDate(endDate, formatError).map(_ => ())
    if (!includeRangeValidation) {
      combine(
        toDateValidation,
        validatedEndDate.andThen(isDateWithinRange(_, formatError))
      )
    } else {
      validatedEndDate match {
        case Valid(endDate) =>
          val res = ResolveIsoDate(startDate) match {
            case Valid(date) => resolveRange(date, endDate)
            case Invalid(_)  => toDateValidation
          }
          res.andThen(_ => isDateWithinRange(endDate, formatError))
        case Invalid(_) => Invalid(List(formatError))
      }
    }
  }

  private def isDateWithinRange(date: LocalDate, error: MtdError): Validated[Seq[MtdError], Unit] = {
    if (date.getYear >= minYear && date.getYear < maxYear) Valid(()) else Invalid(List(error))
  }

  private def validatePeriodDetails(details: PeriodDetails): Validated[Seq[MtdError], Unit] =
    (
      validateAmount(RuleDeductionAmountError, details.deductionAmount),
      validateAmount(RuleCostOfMaterialsError, details.costOfMaterials),
      validateAmount(RuleGrossAmountError, details.grossAmountPaid),
      ResolveIsoDate(details.deductionFromDate, Some(DeductionFromDateFormatError), None).andThen(isDateWithinRange(_, DeductionFromDateFormatError)),
      resolveToDate(details.deductionFromDate, details.deductionToDate, DeductionToDateFormatError)
    ).mapN((_, _, _, _, _) => ())

  def validateBusinessRules(parsed: CreateRequestData): Validated[Seq[MtdError], CreateRequestData] = {
    import parsed.body.periodData

    if (periodData.isEmpty) {
      Invalid(List(RuleIncorrectOrEmptyBodyError))
    } else {
      val result: Validated[Seq[MtdError], CreateRequestData] =
        periodData
          .traverse(periodDetail => validatePeriodDetails(periodDetail))
          .map(_ => parsed)

      val bodyValidation: Validated[Seq[MtdError], CreateRequestData] = combine(
        ResolveIsoDate(parsed.body.fromDate, Some(FromDateFormatError), None).andThen(isDateWithinRange(_, FromDateFormatError)),
        resolveEndDate(parsed.body.fromDate, parsed.body.toDate, ToDateFormatError),
        ResolveEmployeeRef.apply(parsed.body.employerRef, None, None),
        result
      ).map(_ => parsed)

      bodyValidation
    }
  }

}
