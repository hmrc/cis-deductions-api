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

package v2.controllers.validators

import api.controllers.resolvers.{ResolveNino, ResolveSource}
import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated._
import cats.implicits._
import v2.controllers.resolvers.ResolveTaxYear
import v2.models.request.retrieve.RetrieveRequestData

import javax.inject.Singleton

@Singleton
class RetrieveValidatorFactory {

  def validator(nino: String, taxYear: String, source: String): Validator[RetrieveRequestData] = {

    new Validator[RetrieveRequestData] {

      def validate: Validated[Seq[MtdError], RetrieveRequestData] =
        (
          ResolveNino(nino),
          ResolveTaxYear(taxYear),
          ResolveSource(source)
        ).mapN(RetrieveRequestData)

    }
  }

}
