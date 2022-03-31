/*
 * Copyright 2022 HM Revenue & Customs
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
import v1.controllers.requestParsers.validators.validations._
import v1.models.errors._
import v1.models.request.create.{CreateBody, CreateRawData}

class CreateValidator @Inject() (appConfig: AppConfig) extends Validator[CreateRawData] with FixedConfig {

  private val validationSet = List(
    parameterFormatValidator,
    bodyFormatValidator,
    bodyRuleValidator,
    businessRuleValidator
  )

  private def parameterFormatValidator: CreateRawData => List[List[MtdError]] = { data =>
    List(
      NinoValidation.validate(data.nino)
    )
  }

  private def bodyFormatValidator: CreateRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateBody](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyRuleValidator: CreateRawData => List[List[MtdError]] = { data =>
    val req = data.body.as[CreateBody]

    List(
      PeriodDataPositiveAmountValidation.validate(data.body, "deductionAmount", RuleDeductionAmountError),
      PeriodDataPositiveAmountValidation.validate(data.body, "costOfMaterials", RuleCostOfMaterialsError),
      PeriodDataPositiveAmountValidation.validate(data.body, "grossAmountPaid", RuleGrossAmountError),
      PeriodDataDeductionDateValidation.validateDate(data.body, "deductionFromDate", DeductionFromDateFormatError),
      PeriodDataDeductionDateValidation.validateDate(data.body, "deductionToDate", DeductionToDateFormatError),
      DateValidation.validate(FromDateFormatError)(req.fromDate),
      DateValidation.validate(ToDateFormatError)(req.toDate),
      MinTaxYearValidation.validate(req.fromDate, appConfig.minTaxYearCisDeductions.toInt),
      EmployerRefValidation.validate(req.employerRef)
    )
  }

  private def businessRuleValidator: CreateRawData => List[List[MtdError]] = { data =>
    val req      = data.body.as[CreateBody]
    val fromDate = req.fromDate
    val toDate   = req.toDate
    val taxYearValidations: List[List[MtdError]] = List(
      TaxYearDatesValidation.validate(req.fromDate, req.toDate, allowedNumberOfYearsBetweenDates = 1, validateTaxYearEndedFlag = true)
    )
    val periodDataCheck: List[List[MtdError]] = if (taxYearValidations.flatten.isEmpty) {
      req.periodData.map { period =>
        PeriodDataDeductionDateValidation.validateDateOrder(period.deductionFromDate, period.deductionToDate)
      }.toList
    } else {
      taxYearValidations
    }

    if (periodDataCheck.flatten.isEmpty) {
      req.periodData.map { period =>
        PeriodDataDeductionDateValidation.validatePeriodInsideTaxYear(fromDate, toDate, period.deductionFromDate, period.deductionFromDate)
      }.toList
    } else {
      periodDataCheck
    }

  }

  override def validate(data: CreateRawData): List[MtdError] = run(validationSet, data).distinct
}
