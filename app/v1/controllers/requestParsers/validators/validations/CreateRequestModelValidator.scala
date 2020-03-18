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

import config.FixedConfig
import v1.controllers.requestParsers.validators.Validator
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import v1.models.request.{CreateRawData, CreateRequestData, CreateRequestModel}
import v1.models.requestData.SampleRequestData

class CreateRequestModelValidator extends Validator[CreateRawData] with FixedConfig {

  private val validationSet = List(
    parameterFormatValidator,
    bodyFormatValidator

  )

  private def parameterFormatValidator: CreateRawData => List[List[MtdError]] = { data =>

    List(
      NinoValidation.validate(data.nino)
    )
  }


  private def bodyFormatValidator: CreateRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateRequestModel](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  override def validate(data: CreateRawData): List[MtdError] = run(validationSet, data)
}
