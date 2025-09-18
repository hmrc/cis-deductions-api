/*
 * Copyright 2025 HM Revenue & Customs
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

package models.errors

import shared.models.errors.MtdError

import play.api.http.Status._

object SubmissionIdFormatError extends MtdError("FORMAT_SUBMISSION_ID", "The provided submission ID is invalid", BAD_REQUEST)

object RuleUnalignedDeductionsPeriodError
    extends MtdError("RULE_UNALIGNED_DEDUCTIONS_PERIOD", "The deductions periods do not align with the tax year supplied", BAD_REQUEST)

object RuleDuplicatePeriodError
    extends MtdError("RULE_DUPLICATE_PERIOD", "More than one deduction period has been supplied for the same month or period", BAD_REQUEST)

object RuleDeductionsDateRangeInvalidError
    extends MtdError(
      "RULE_DEDUCTIONS_DATE_RANGE_INVALID",
      "The deductions period must align from the 6th of one month to the 5th of the following month",
      BAD_REQUEST)

object RuleDateRangeOutOfDateError
    extends MtdError(
      "RULE_DATE_RANGE_OUT_OF_DATE",
      "The specified date range is outside the allowable tax years (the current tax year minus four years)",
      BAD_REQUEST)

object RuleSourceInvalidError extends MtdError("RULE_SOURCE_INVALID", "The source is invalid", BAD_REQUEST)

object RuleDuplicateSubmissionError extends MtdError("RULE_DUPLICATE_SUBMISSION", "CIS deduction already exists for this tax year", BAD_REQUEST)

object EmployerRefFormatError extends MtdError("FORMAT_EMPLOYER_REFERENCE", "The format of the Employer Reference number is invalid", BAD_REQUEST)

object RuleDeductionAmountError
    extends MtdError(
      "RULE_DEDUCTIONS_AMOUNT",
      "The deductions amount should be a positive number less than 99999999999.99 up to 2 decimal places",
      BAD_REQUEST)

object RuleMissingFromDateError extends MtdError("MISSING_FROM_DATE", "The From date parameter is missing", BAD_REQUEST)

object RuleCostOfMaterialsError
    extends MtdError(
      "RULE_COST_OF_MATERIALS",
      "The cost of materials should be a positive number less than 99999999999.99 up to 2 decimal places",
      BAD_REQUEST)

object RuleGrossAmountError
    extends MtdError(
      "RULE_GROSS_AMOUNT_PAID",
      "The gross amount should be a positive number less than 99999999999.99 up to 2 decimal places",
      BAD_REQUEST)

object RuleOutsideAmendmentWindowError
    extends MtdError(
      code = "RULE_OUTSIDE_AMENDMENT_WINDOW",
      message = "You are outside the amendment window",
      BAD_REQUEST
    )
