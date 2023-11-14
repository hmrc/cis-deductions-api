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
import play.api.libs.json.{JsValue, OFormat, Reads}
import shared.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import utils.EmptinessChecker
import utils.EmptyPathsResult.{CompletelyEmpty, EmptyPaths, NoEmptyPaths}

class ResolveNonEmptyJsonObject[T: OFormat : EmptinessChecker]()(implicit val reads: Reads[T])
  extends Resolver[JsValue, T]
    with JsonObjectResolving[T] {

  def apply(data: JsValue, error: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], T] =
    validateAndCheckNonEmpty(data).leftMap(schemaErrors => withErrors(error, schemaErrors, path))

  private def validateAndCheckNonEmpty(data: JsValue): Validated[Seq[MtdError], T] = {
    validate(data) match {
      case Invalid(schemaErrors) =>
        Invalid(schemaErrors)

      case Valid(parsedBody) =>
        EmptinessChecker.findEmptyPaths(parsedBody) match {
          case CompletelyEmpty   => Invalid(List(RuleIncorrectOrEmptyBodyError))
          case EmptyPaths(paths) => Invalid(List(RuleIncorrectOrEmptyBodyError.withPaths(paths)))
          case NoEmptyPaths      => Valid(parsedBody)
        }
    }
  }

}
