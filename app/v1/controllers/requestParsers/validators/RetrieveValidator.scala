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

import config.{AppConfig, FixedConfig}
import javax.inject.Inject
import v1.controllers.requestParsers.validators.validations.{MinTaxYearValidation, _}
import v1.models.errors._
import v1.models.request.retrieve.RetrieveRawData

class RetrieveValidator @Inject()(appConfig: AppConfig) extends Validator[RetrieveRawData] with FixedConfig{

  private val validationSet = List(mandatoryFieldValidation, parameterFormatValidation, businessRuleValidator)

  private def parameterFormatValidation: RetrieveRawData => List[List[MtdError]] = (data: RetrieveRawData) => List(
    NinoValidation.validate(data.nino),
    SourceValidation.validate(data.source),
    DateValidation.validate(FromDateFormatError)(data.fromDate.get),
    DateValidation.validate(ToDateFormatError)(data.toDate.get),
    MinTaxYearValidation.validate(data.toDate.get, appConfig.minTaxYearCisDeductions.toInt)
  )

  private def mandatoryFieldValidation: RetrieveRawData => List[List[MtdError]] = (data: RetrieveRawData) => List(
    MandatoryValidation.validate(RuleMissingFromDateError)(data.fromDate),
    MandatoryValidation.validate(RuleMissingToDateError)(data.toDate)
  )

  private def businessRuleValidator: RetrieveRawData => List[List[MtdError]] = { data =>
    List(
      TaxYearDatesValidation.validate(data.fromDate.get, data.toDate.get, Some(1))
    )
  }

  override def validate(data: RetrieveRawData): List[MtdError] = run(validationSet, data).distinct
}
