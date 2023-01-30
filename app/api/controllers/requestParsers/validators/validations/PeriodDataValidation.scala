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

package api.controllers.requestParsers.validators.validations

import api.controllers.requestParsers.validators.validations.validations.NoValidationErrors
import api.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import play.api.libs.json.JsValue

object PeriodDataValidation {

  def emptyPeriodDataValidation(json: JsValue): List[MtdError] = {
    val periodData = (json \ "periodData").as[List[JsValue]]

    if (periodData.isEmpty) List(RuleIncorrectOrEmptyBodyError) else NoValidationErrors
  }

}