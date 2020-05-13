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
import v1.controllers.requestParsers.validators.validations._
import v1.models.errors._
import v1.models.request.ListDeductionsRawData

class ListDeductionsValidator extends Validator[ListDeductionsRawData] with FixedConfig{

  private val validationSet = List(mandatoryFieldValidation, parameterFormatValidation, businessRuleValidator)

  private def parameterFormatValidation: ListDeductionsRawData => List[List[MtdError]] = (data: ListDeductionsRawData) => List(
    NinoValidation.validate(data.nino),
    SourceValidation.validate(data.source),
    ToBeforeFromDateValidation.validate(data.fromDate.get, data.toDate.get, RuleDateRangeInvalidError)
  )

  private def mandatoryFieldValidation: ListDeductionsRawData => List[List[MtdError]] = (data: ListDeductionsRawData) => List(
    MandatoryValidation.validate(RuleMissingFromDateError)(data.fromDate),
    MandatoryValidation.validate(RuleMissingToDateError)(data.toDate)
  )

  private def businessRuleValidator: ListDeductionsRawData => List[List[MtdError]] = { data =>
    List(
      TaxYearDatesValidation.validate(data.fromDate.get, data.toDate.get, Some(1))
    )
  }

  override def validate(data: ListDeductionsRawData): List[MtdError] = run(validationSet, data).distinct
}
