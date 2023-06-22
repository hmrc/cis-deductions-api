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

package v2.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations.{NinoValidation, DateValidation}
import api.models.errors._
import config.{AppConfig, FixedConfig}
import v2.controllers.requestParsers.validators.validations.SourceValidation
import v2.models.request.retrieve.RetrieveRawData

import javax.inject.Inject

class RetrieveValidator @Inject() (appConfig: AppConfig) extends Validator[RetrieveRawData] with FixedConfig {

  private val validationSet = List(parameterFormatValidation)

  private def parameterFormatValidation: RetrieveRawData => List[List[MtdError]] = (data: RetrieveRawData) =>
    List(
      NinoValidation.validate(data.nino),
      DateValidation.validate(TaxYearFormatError)(data.taxYear),
      SourceValidation.validate(data.source)
    )

  override def validate(data: RetrieveRawData): List[MtdError] = run(validationSet, data).distinct
}
