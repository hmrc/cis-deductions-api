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

package v3.controllers.validators

import cats.data.Validated
import cats.implicits.*
import play.api.libs.json.{JsString, JsValue}
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.*
import shared.models.domain.TaxYear
import shared.models.errors.*
import v3.controllers.validators.DeductionsValidator.*
import v3.controllers.validators.resolvers.ResolveSubmissionId
import v3.models.errors.CisDeductionsApiCommonErrors.DeductionToDateFormatError
import v3.models.request.amend.{AmendBody, AmendRequestData}

import javax.inject.Singleton

@Singleton
class AmendValidatorFactory {

  private val resolveJson = new ResolveNonEmptyJsonObject[AmendBody]()

  def validator(nino: String, submissionId: String, body: JsValue): Validator[AmendRequestData] =
    new Validator[AmendRequestData] {

      private val resolveTaxYearFromPeriodDetails = {
        def deductionToDates(body: JsValue): Seq[String] =
          (body \\ "deductionToDate").collect { case JsString(isoDate) => isoDate }.toSeq

        val resolveToDateFromPeriodDetails: Resolver[JsValue, String] =
          deductionToDates(_).headOption.toValid(List(RuleIncorrectOrEmptyBodyError))

        val resolveTaxYearFromIsoDate = ResolveIsoDate(DeductionToDateFormatError).resolver.map(TaxYear.containing)

        resolveToDateFromPeriodDetails.thenResolve(resolveTaxYearFromIsoDate)
      }

      def validate: Validated[Seq[MtdError], AmendRequestData] =
        (
          ResolveNino(nino),
          ResolveSubmissionId(submissionId),
          resolveTaxYearFromPeriodDetails(body),
          resolveJson(body)
        ).mapN(AmendRequestData.apply) andThen validateBusinessRules

      private def validateBusinessRules(parsed: AmendRequestData): Validated[Seq[MtdError], AmendRequestData] =
        validatePeriodData(parsed.body.periodData).map(_ => parsed)

    }

}
