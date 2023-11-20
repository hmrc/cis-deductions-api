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
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import shared.controllers.validators.resolvers._
import shared.controllers.validators.{RulesValidator, Validator, resolvers}
import shared.models.domain.TaxYear
import shared.models.errors._
import v1.controllers.validators.resolvers.ResolveSubmissionId
import v1.models.errors.CisDeductionsApiCommonErrors.{DeductionFromDateFormatError, DeductionToDateFormatError}
import v1.models.request.amend.{AmendBody, AmendRequestData, PeriodDetails}

import java.time.LocalDate
import javax.inject.Singleton

@Singleton
class AmendValidatorFactory extends RulesValidator[AmendRequestData] {

  private val minYear = 1900
  private val maxYear = 2100

  private val resolveJson = new ResolveNonEmptyJsonObject[AmendBody]()

  def validator(nino: String, submissionId: String, body: JsValue): Validator[AmendRequestData] =
    new Validator[AmendRequestData] {

      private def resolveTaxYearFromPeriodDetails(body: JsValue): Validated[Seq[MtdError], TaxYear] = {
        (body \ "periodData").validateOpt[Seq[JsValue]] match {
          case JsSuccess(Some(periodData), _) if periodData.nonEmpty =>
            (periodData.head \ "deductionToDate").validate[String] match {
              case JsSuccess(date, _) =>
                ResolveIsoDate(DeductionToDateFormatError)(date) match {
                  case Valid(isoDate) =>
                    val taxYearStr = TaxYear.fromIso(isoDate.toString).asMtd
                    ResolveTaxYear(taxYearStr) match {
                      case Valid(taxYear) => Valid(taxYear)
                      case Invalid(_)     => Invalid(List(DeductionToDateFormatError))
                    }
                  case Invalid(_) => Invalid(List(DeductionToDateFormatError))
                }
              case JsError(_) => Invalid(List(RuleIncorrectOrEmptyBodyError))
            }
          case JsSuccess(_, _) | JsError(_) => Invalid(List(RuleIncorrectOrEmptyBodyError))
        }
      }

      def validate: Validated[Seq[MtdError], AmendRequestData] =
        (
          ResolveNino(nino),
          ResolveSubmissionId(submissionId),
          resolveTaxYearFromPeriodDetails(body),
          resolveJson(body)
        ).mapN(AmendRequestData.apply) andThen validateBusinessRules

    }

  def resolveNumeric(error: MtdError, maybeValue: Option[BigDecimal]): Validated[Seq[MtdError], Unit] =
    maybeValue match {
      case Some(value) => ResolveParsedNumber().resolver(error)(value).map(_ => ())
      case None        => Valid(())
    }

  private def isDateWithinRange(date: LocalDate, error: MtdError): Validated[Seq[MtdError], Unit] =
    if (date.getYear >= minYear && date.getYear < maxYear)
      Valid(())
    else
      Invalid(List(error))

  private val validateDeductionAmount = ResolveParsedNumber().resolver(RuleDeductionAmountError)

  def validatePeriodDetails(details: PeriodDetails): Validated[Seq[MtdError], Unit] = {
    combine(
      validateDeductionAmount(details.deductionAmount),
      resolveNumeric(RuleCostOfMaterialsError, details.costOfMaterials),
      resolveNumeric(RuleGrossAmountError, details.grossAmountPaid),
      ResolveIsoDate(details.deductionToDate, DeductionToDateFormatError).andThen(isDateWithinRange(_, DeductionToDateFormatError)),
      resolvers.ResolveIsoDate(details.deductionFromDate, DeductionFromDateFormatError).andThen(isDateWithinRange(_, DeductionFromDateFormatError))
    )
  }

  def validateBusinessRules(parsed: AmendRequestData): Validated[Seq[MtdError], AmendRequestData] = {
    import parsed.body.periodData
    if (periodData.isEmpty)
      Invalid(List(RuleIncorrectOrEmptyBodyError))
    else
      periodData.traverse(validatePeriodDetails).map(_ => parsed)
  }

}
