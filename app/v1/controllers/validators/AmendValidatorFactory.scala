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
import api.controllers.validators.{RulesValidator, Validator}
import api.models.domain.TaxYear
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import v1.controllers.resolvers.ResolveTaxYear
import v1.models.request.amend.{AmendBody, AmendRequestData, PeriodDetails}

import java.time.LocalDate
import javax.inject.Singleton
import scala.annotation.nowarn

@Singleton
class AmendValidatorFactory extends RulesValidator[AmendRequestData] {

  private val minYear = 1900
  private val maxYear = 2100

  @nowarn("cat=lint-byname-implicit")
  private val resolveJson = new ResolveNonEmptyJsonObject[AmendBody]()

  def validator(nino: String, submissionId: String, body: JsValue): Validator[AmendRequestData] =
    new Validator[AmendRequestData] {

      private def resolveTaxYearFromPeriodDetails(body: JsValue): Validated[Seq[MtdError], TaxYear] = {
        (body \ "periodData").validateOpt[Seq[JsValue]] match {
          case JsSuccess(Some(periodData), _) if periodData.nonEmpty =>
            (periodData.head \ "deductionToDate").validate[String] match {
              case JsSuccess(date, _) =>
                ResolveIsoDate(date) match {
                  case Valid(isoDate) =>
                    val taxYearStr = TaxYear.fromIso(isoDate.toString).asMtd
                    ResolveTaxYear(taxYearStr) match {
                      case Valid(taxYear) => Valid(taxYear)
                      case Invalid(_)     => Invalid(Seq(DeductionToDateFormatError))
                    }
                  case Invalid(_) => Invalid(Seq(DeductionToDateFormatError))
                }
              case JsError(_) => Invalid(Seq(RuleIncorrectOrEmptyBodyError))
            }
          case JsSuccess(_, _) | JsError(_) => Invalid(Seq(RuleIncorrectOrEmptyBodyError))
        }
      }

      def validate: Validated[Seq[MtdError], AmendRequestData] =
        (
          ResolveNino(nino),
          ResolveSubmissionId(submissionId),
          resolveTaxYearFromPeriodDetails(body),
          resolveJson(body, Some(RuleIncorrectOrEmptyBodyError), None)
        ).mapN(AmendRequestData.apply) andThen validateBusinessRules

    }

  def resolveNumeric(error: MtdError, value: Option[BigDecimal]): Validated[Seq[MtdError], Unit] =
    ResolveAmount().apply(value, Some(error), None).map(_ => ())

  private def isDateWithinRange(date: LocalDate, error: MtdError): Validated[Seq[MtdError], Unit] = {
    if (date.getYear >= minYear && date.getYear < maxYear) Valid(()) else Invalid(List(error))
  }

  def validatePeriodDetails(details: PeriodDetails): Validated[Seq[MtdError], Unit] =
    (
      ResolveAmount().apply(details.deductionAmount, Some(RuleDeductionAmountError), None),
      resolveNumeric(RuleCostOfMaterialsError, details.costOfMaterials),
      resolveNumeric(RuleGrossAmountError, details.grossAmountPaid),
      ResolveIsoDate(details.deductionToDate, DeductionToDateFormatError).andThen(isDateWithinRange(_, DeductionToDateFormatError)),
      ResolveIsoDate(details.deductionFromDate, DeductionFromDateFormatError).andThen(isDateWithinRange(_, DeductionFromDateFormatError))
    ).mapN((_, _, _, _, _) => ())

  def validateBusinessRules(parsed: AmendRequestData): Validated[Seq[MtdError], AmendRequestData] = {
    val body: Seq[PeriodDetails] = parsed.body.periodData
    if (body.isEmpty) {
      Invalid(List(RuleIncorrectOrEmptyBodyError))
    } else {
      body.traverse(validatePeriodDetails).map(_ => parsed)
    }
  }

}
