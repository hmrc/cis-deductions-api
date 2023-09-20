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

import api.controllers.resolvers.{ResolveNino, ResolveSubmissionId}
import api.controllers.validators.Validator
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.Valid
import cats.implicits.catsSyntaxTuple3Semigroupal
import config.AppConfig
import v1.controllers.resolvers.ResolveTysTaxYear
import v1.models.request.delete.DeleteRequestData

import javax.inject.Inject

class DeleteValidatorFactory @Inject() (appConfig: AppConfig) {

  def validator(nino: String, submissionId: String, taxYear: Option[String]): Validator[DeleteRequestData] =
    new Validator[DeleteRequestData] {

      def resolveTaxYear(value: Option[String]): Validated[Seq[MtdError], Option[TaxYear]] = {
        value match {
          case Some(taxYear) =>
            ResolveTysTaxYear.apply(Some(taxYear), None, None)

          case _ => Valid(None)
        }

      }

      def validate: Validated[Seq[MtdError], DeleteRequestData] =
        (
          ResolveNino(nino),
          ResolveSubmissionId(submissionId),
          resolveTaxYear(taxYear)
        ).mapN(DeleteRequestData)

    }

}
