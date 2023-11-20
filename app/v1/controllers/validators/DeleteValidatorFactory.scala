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
import cats.implicits.catsSyntaxTuple3Semigroupal
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveTysTaxYear}
import shared.models.errors.MtdError
import v1.controllers.validators.resolvers.ResolveSubmissionId
import v1.models.request.delete.DeleteRequestData

class DeleteValidatorFactory {

  def validator(nino: String, submissionId: String, taxYear: Option[String]): Validator[DeleteRequestData] =
    new Validator[DeleteRequestData] {

      def validate: Validated[Seq[MtdError], DeleteRequestData] =
        (
          ResolveNino(nino),
          ResolveSubmissionId(submissionId),
          ResolveTysTaxYear(taxYear)
        ).mapN(DeleteRequestData)

    }

}
