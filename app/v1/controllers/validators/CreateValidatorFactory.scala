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
  // private val resolveTaxYearDates = ResolveTaxYearDates()

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

//      private def resolveRange(startDate: LocalDate, endDate: LocalDate): Validated[Seq[MtdError], String] = {
//        resolveDateRange(startDate.toString -> endDate.toString)
//        //   val dateRangeValid    = resolveDateRange(startDate.toString -> endDate.toString).isValid
//        //  val taxYearRangeValid = resolveTaxYearDates(startDate.toString -> endDate.toString).isValid
//
////    if (dateRangeValid && taxYearRangeValid)
//        Valid(endDate.toString)
//        //  else
//        //  Invalid(List(RuleDateRangeInvalidError))
//      }

//  private def validateEndDate(startDate: String, endDate: String, formatError: MtdError): Validated[Seq[MtdError], Unit] = {
//    ResolveMinTaxYear((startDate, appConfig.minTaxYearCisDeductions.toInt), Some(RuleTaxYearNotSupportedError), None) match {
//      case Valid(_) =>
//        validateToDate(startDate, endDate, formatError, includeRangeValidation = true).map(_ => ())
//      case Invalid(error) => Invalid(error)
//    }
//  }
//
//  private def validateToDate(startDate: String,
//                            endDate: String,
//                            formatError: MtdError,
//                            includeRangeValidation: Boolean = false): Validated[Seq[MtdError], Unit] = {
//
//    val validatedEndDate = ResolveIsoDate(endDate, formatError)
//    val validatedToDate = ResolveDate(endDate, formatError).map(_ => ())
//    if (!includeRangeValidation) {
//      combine(
//        validatedToDate,
//        validatedEndDate.andThen(isDateWithinRange(_, formatError))
//      )
//    } else {
//      validatedEndDate match {
//        case Valid(endDate) =>
//          val res = ResolveIsoDate(startDate) match {
//            case Valid(date) => resolveRange(date, endDate)
//            case Invalid(_)  => validatedToDate
//          }
//          res.andThen(_ => isDateWithinRange(endDate, formatError))
//        case Invalid(_) => Invalid(List(formatError))
//      }
//    }
//  }
//
//      private def isDateWithinRange(date: LocalDate, error: MtdError): Validated[Seq[MtdError], Unit] = {
//        if (date.getYear >= minYear && date.getYear < maxYear) Valid(()) else Invalid(List(error))
//      }

      private def validatePeriodDetails(details: PeriodDetails): Validated[Seq[MtdError], Unit] = {
        combine(
          validateAmount(RuleDeductionAmountError, details.deductionAmount),
          validateAmount(RuleCostOfMaterialsError, details.costOfMaterials),
          validateAmount(RuleGrossAmountError, details.grossAmountPaid),
          resolveDeductionDateRange(details.deductionFromDate -> details.deductionToDate)
          //   ResolveIsoDate(details.deductionFromDate, Some(DeductionFromDateFormatError), None).andThen(isDateWithinRange(_, DeductionFromDateFormatError)),
          //   validateToDate(details.deductionFromDate, details.deductionToDate, DeductionToDateFormatError)
        ).map(_ => ())
      }

    }

}
