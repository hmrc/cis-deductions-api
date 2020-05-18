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
import v1.models.request.{CreateRawData, CreateRequest}

class CreateValidator extends Validator[CreateRawData] with FixedConfig {
  private val validationSet = List(
    parameterFormatValidator,
    bodyFormatValidator,
    bodyRuleValidator
  )

  private def parameterFormatValidator: CreateRawData => List[List[MtdError]] = { data =>
    List(
      NinoValidation.validate(data.nino)
    )
  }

  private def bodyFormatValidator: CreateRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateRequest](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyRuleValidator: CreateRawData => List[List[MtdError]] = { data =>
    val req = data.body.as[CreateRequest]

    List(
      PeriodDataPositiveAmountValidation.validate(data.body, "deductionAmount", RuleDeductionAmountError),
      PeriodDataPositiveAmountValidation.validate(data.body, "costOfMaterials", RuleCostOfMaterialsError),
      PeriodDataPositiveAmountValidation.validate(data.body, "grossAmountPaid", RuleGrossAmountError),
      PeriodDataDeductionDateValidation.validate(data.body, "deductionFromDate", DeductionFromDateFormatError),
      PeriodDataDeductionDateValidation.validate(data.body, "deductionToDate", DeductionToDateFormatError),
      DateValidation.validate(FromDateFormatError)(req.fromDate),
      DateValidation.validate(ToDateFormatError)(req.toDate),
      ToBeforeFromDateValidation.validate(req.fromDate, req.toDate, RuleDateRangeInvalidError)
    )
  }

  override def validate(data: CreateRawData): List[MtdError] = run(validationSet, data).distinct
}
