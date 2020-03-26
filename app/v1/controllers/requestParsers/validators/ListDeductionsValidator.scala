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

package v1.controllers.requestParsers.validators

import config.FixedConfig
import v1.controllers.requestParsers.validators.validations.{DateValidation, NinoValidation, SourceValidation, ToBeforeFromDateValidation}
import v1.models.errors.{FromDateFormatError, MtdError, RuleDateRangeInvalidError, ToDateFormatError}
import v1.models.request.ListDeductionsRawData

class ListDeductionsValidator extends Validator[ListDeductionsRawData] with FixedConfig{

  private val validationSet = List(parameterFormatValidation)

  private def parameterFormatValidation: ListDeductionsRawData => List[List[MtdError]] = (data: ListDeductionsRawData) => List(
    NinoValidation.validate(data.nino),
    SourceValidation.validate(data.source),
    DateValidation.validate(FromDateFormatError)(data.fromDate),
    DateValidation.validate(ToDateFormatError)(data.toDate),
    ToBeforeFromDateValidation.validate(data.fromDate, data.toDate, RuleDateRangeInvalidError)
  )

  override def validate(data: ListDeductionsRawData): List[MtdError] = run(validationSet, data).distinct
}
