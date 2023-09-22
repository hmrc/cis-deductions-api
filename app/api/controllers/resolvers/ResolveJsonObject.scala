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

import api.models.errors.MtdError
import cats.data.Validated
import play.api.libs.json._

class ResolveJsonObject[T](implicit val reads: Reads[T]) extends Resolver[JsValue, T] with JsonObjectResolving[T] {

  def apply(data: JsValue, error: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], T] =
    validate(data).leftMap(errs => withErrors(error, errs, path))

}
