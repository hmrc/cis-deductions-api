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

import api.controllers.validators.resolvers.*
import api.models.domain.{DateRange, TaxYear}
import api.models.errors.*
import cats.data.Validated
import cats.implicits.{catsSyntaxTuple2Semigroupal, toTraverseOps}
import models.errors.*
import v3.models.errors.CisDeductionsApiCommonErrors.*
import v3.models.request.amend.PeriodDetails

import java.time.LocalDate

object DeductionsValidator extends ResolverSupport {

  private[validators] val minYear = 1900
  private[validators] val maxYear = 2099

  private val resolveAmount = ResolveParsedNumber()

  private[validators] case class ParsedPeriod(dateRange: DateRange, index: Int)

  private[validators] val checkDateRangeIsAFullTaxYear: Validator[DateRange] = satisfies(RuleDateRangeInvalidError) { (dateRange: DateRange) =>
    val taxYear: TaxYear = TaxYear.containing(dateRange.endDate)

    (taxYear.startDate, taxYear.endDate) == (dateRange.startDate, dateRange.endDate)
  }

  private[validators] def validatePeriodData(allPeriodDetails: Seq[PeriodDetails]): Validated[Seq[MtdError], Seq[ParsedPeriod]] =
    allPeriodDetails.zipWithIndex.traverse { case (detail, index) =>
      (
        validatePeriodAmounts(index)(detail),
        resolveDeductionDateRange(detail, index)
      ).mapN { (_, dateRange) =>
        ParsedPeriod(dateRange, index)
      }
    }

  private[validators] def validateDeductionDateRanges(periods: Seq[ParsedPeriod]): Validated[Seq[MtdError], Unit] = {
    val paths: Seq[String] = periods.flatMap { period =>
      val fromDate: LocalDate = period.dateRange.startDate
      val toDate: LocalDate   = period.dateRange.endDate

      if (fromDate.getDayOfMonth != 6 || toDate != fromDate.plusMonths(1).withDayOfMonth(5)) {
        List(s"${periodBasePath(period.index)}/deductionFromDate", s"${periodBasePath(period.index)}/deductionToDate")
      } else {
        Nil
      }
    }

    Validated.cond(paths.isEmpty, (), List(RuleDeductionsDateRangeInvalidError.withPaths(paths)))
  }

  private[validators] def validateDuplicatePeriods(periods: Seq[ParsedPeriod]): Validated[Seq[MtdError], Unit] = {
    val duplicateErrors: Seq[MtdError] = periods
      .groupMap(_.dateRange)(period => periodBasePath(period.index))
      .collect {
        case (dateRange, paths) if paths.size > 1 => RuleDuplicatePeriodError.forDuplicatedPeriod(dateRange, paths)
      }
      .toSeq

    Validated.cond(duplicateErrors.isEmpty, (), duplicateErrors)
  }

  private def validatePeriodAmounts(index: Int): Resolver[PeriodDetails, PeriodDetails] =
    resolveValid[PeriodDetails].thenValidate(
      combinedValidator(
        validateAmount(RuleDeductionAmountError.withPath(s"${periodBasePath(index)}/deductionAmount")).contramap(_.deductionAmount),
        validateMaybeAmount(RuleCostOfMaterialsError.withPath(s"${periodBasePath(index)}/costOfMaterials")).contramap(_.costOfMaterials),
        validateMaybeAmount(RuleGrossAmountError.withPath(s"${periodBasePath(index)}/grossAmountPaid")).contramap(_.grossAmountPaid)
      )
    )

  private def validateMaybeAmount(error: => MtdError): Validator[Option[BigDecimal]] = validateAmount(error).validateOptionally

  private def validateAmount(error: => MtdError): Validator[BigDecimal] = resolveAmount.validator(error)

  private def resolveDeductionDateRange(detail: PeriodDetails, index: Int): Validated[Seq[MtdError], DateRange] =
    ResolveDateRange(
      startDateFormatError = DeductionFromDateFormatError.withPath(s"${periodBasePath(index)}/deductionFromDate"),
      endDateFormatError = DeductionToDateFormatError.withPath(s"${periodBasePath(index)}/deductionToDate"),
      endBeforeStartDateError = RuleDateRangeInvalidError.withPath(periodBasePath(index))
    ).withYearsLimitedTo(minYear, maxYear)(detail.deductionFromDate -> detail.deductionToDate)

  private def periodBasePath(index: Int): String = s"/periodData/$index"

}
