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

package api.controllers.resolvers

import api.models.domain.CalculationId
import api.models.errors.{CalculationIdFormatError, MtdError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

object ResolveCalculationId extends Resolver[String, CalculationId] {

  private val calculationIdRegex = """^[0-9]{8}|[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$""".r

  def apply(value: String, unusedError: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], CalculationId] = {
    if (calculationIdRegex.matches(value)) { Valid(CalculationId(value)) }
    else { Invalid(List(CalculationIdFormatError.maybeWithExtraPath(path))) }
  }

}
