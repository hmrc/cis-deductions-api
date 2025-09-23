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
import cats.data.Validated.Invalid
import cats.implicits.toTraverseOps
import models.errors.{RuleCostOfMaterialsError, RuleDeductionAmountError, RuleGrossAmountError}
import shared.controllers.validators.resolvers.*
import shared.models.domain.{DateRange, TaxYear}
import shared.models.errors.*
import v2.models.errors.CisDeductionsApiCommonErrors.*
import v2.models.request.amend.PeriodDetails

object DeductionsValidator extends ResolverSupport {

  private[validators] val minYear = 1900
  private[validators] val maxYear = 2099

  val checkDateRangeIsAFullTaxYear: Validator[DateRange] = satisfies(RuleDateRangeInvalidError) { (dateRange: DateRange) =>
    val taxYear = TaxYear.containing(dateRange.endDate)

    (taxYear.startDate, taxYear.endDate) == (dateRange.startDate, dateRange.endDate)
  }

  private val resolveAmount = ResolveParsedNumber()

  def validatePeriodData(allPeriodDetails: Seq[PeriodDetails]): Validated[Seq[MtdError], Unit] = {
    if (allPeriodDetails.isEmpty)
      Invalid(List(RuleIncorrectOrEmptyBodyError))
    else
      allPeriodDetails.traverse(validatePeriodDetails).map(_ => ())
  }

  private val validatePeriodDetails =
    resolveValid[PeriodDetails] thenValidate combinedValidator(
      validateAmount(RuleDeductionAmountError).contramap(_.deductionAmount),
      validateMaybeAmount(RuleCostOfMaterialsError).contramap(_.costOfMaterials),
      validateMaybeAmount(RuleGrossAmountError).contramap(_.grossAmountPaid),
      validateIsoDate(DeductionToDateFormatError).contramap(_.deductionToDate),
      validateIsoDate(DeductionFromDateFormatError).contramap(_.deductionFromDate)
    )

  private def validateMaybeAmount(error: => MtdError): Validator[Option[BigDecimal]] = validateAmount(error).validateOptionally

  private def validateAmount(error: => MtdError): Validator[BigDecimal] = resolveAmount.validator(error)

  private def validateIsoDate(error: => MtdError): Validator[String] =
    ResolveIsoDate(error).resolver
      .map(_.getYear)
      .thenValidate(inRange(minYear, maxYear, error))
      .asValidator

}
