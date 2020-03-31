/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators.validations

import v1.models.errors._

object SourceValidation {

  private val sources = Seq("all","customer","contractor")

  def validate(source: Option[String]): List[MtdError] = {
    source match{
      case Some(x) if sources.contains(Some(x).get) => NoValidationErrors
      case None => NoValidationErrors
      case _ => List(RuleSourceError)
    }
  }
}
