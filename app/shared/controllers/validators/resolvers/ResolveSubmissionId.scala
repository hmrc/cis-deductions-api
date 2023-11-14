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

package shared.controllers.validators.resolvers

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import shared.models.domain.SubmissionId
import shared.models.errors.{MtdError, SubmissionIdFormatError}

object ResolveSubmissionId extends Resolver[String, SubmissionId] {

  private val submissionIdRegex = "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"

  def apply(value: String, unusedError: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], SubmissionId] = {
    if (value.matches(submissionIdRegex)) { Valid(SubmissionId(value)) }
    else { Invalid(List(SubmissionIdFormatError.maybeWithExtraPath(path))) }
  }

}
