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

package v1.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations._
import api.models.errors._
import v1.models.request.amend.{AmendBody, AmendRawData}

class AmendValidator extends Validator[AmendRawData] {

  private val validationSet = List(parameterFormatValidation, bodyFormatValidator, bodyRuleValidator, businessRuleValidator)

  private def parameterFormatValidation: AmendRawData => List[List[MtdError]] = (data: AmendRawData) => {
    List(
      NinoValidation.validate(data.nino),
      SubmissionIdValidation.validate(data.id)
    )
  }

  private def bodyFormatValidator: AmendRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendBody](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyRuleValidator: AmendRawData => List[List[MtdError]] = { data =>
    List(
      PeriodDataPositiveAmountValidation.validate(data.body, "deductionAmount", RuleDeductionAmountError),
      PeriodDataPositiveAmountValidation.validate(data.body, "costOfMaterials", RuleCostOfMaterialsError),
      PeriodDataPositiveAmountValidation.validate(data.body, "grossAmountPaid", RuleGrossAmountError),
      PeriodDataDeductionDateValidation.validateDate(data.body, "deductionFromDate", DeductionFromDateFormatError),
      PeriodDataDeductionDateValidation.validateDate(data.body, "deductionToDate", DeductionToDateFormatError),
      PeriodDataValidation.emptyPeriodDataValidation(data.body)
    )
  }

  private def businessRuleValidator: AmendRawData => List[List[MtdError]] = { data =>
    val req = data.body.as[AmendBody]

    val dateOrderValidations = req.periodData.map { period =>
      PeriodDataDeductionDateValidation.validateDateOrder(period.deductionFromDate, period.deductionToDate)
    }.toList

    val multiplePeriodsTaxYearValidation = List(PeriodDataDeductionDateValidation.validateTaxYearForMultiplePeriods(data))

    dateOrderValidations ++ multiplePeriodsTaxYearValidation
  }

  override def validate(data: AmendRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

}
