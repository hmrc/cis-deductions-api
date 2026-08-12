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
import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import cats.data.Validated
import cats.implicits.*
import config.CisDeductionsApiConfig
import play.api.libs.json.JsValue
import v3.controllers.validators.DeductionsValidator.*
import v3.controllers.validators.resolvers.ResolveSubmissionId
import v3.models.domain.SubmissionId
import v3.models.request.amend.{AmendBody, AmendRequestData}

import javax.inject.{Inject, Singleton}
import scala.math.Ordered.orderingToOrdered

@Singleton
class AmendValidatorFactory @Inject() (appConfig: CisDeductionsApiConfig) {

  private val resolveJson = new ResolveNonEmptyJsonObject[AmendBody]()

  private case class ParsedData(nino: Nino, submissionId: SubmissionId, body: AmendBody)

  def validator(nino: String, submissionId: String, body: JsValue): Validator[AmendRequestData] =
    new Validator[AmendRequestData] {

      def validate: Validated[Seq[MtdError], AmendRequestData] =
        (
          ResolveNino(nino),
          ResolveSubmissionId(submissionId),
          resolveJson(body)
        ).mapN(ParsedData.apply) andThen validateBusinessRules

      private def validateBusinessRules(parsed: ParsedData): Validated[Seq[MtdError], AmendRequestData] =
        validatePeriodData(parsed.body.periodData).andThen { periods =>
          val taxYear: TaxYear = TaxYear.containing(periods.head.dateRange.endDate)

          combine(
            validateTaxYearIsSupported(taxYear),
            validateDeductionDateRanges(periods),
            validateDuplicatePeriods(periods)
          ).map { _ =>
            AmendRequestData(parsed.nino, parsed.submissionId, taxYear, parsed.body)
          }
        }

      private def validateTaxYearIsSupported(taxYear: TaxYear): Validated[Seq[MtdError], Unit] = Validated.cond(
        taxYear >= appConfig.minTaxYearCisDeductions,
        (),
        List(RuleTaxYearNotSupportedError)
      )

    }

}
