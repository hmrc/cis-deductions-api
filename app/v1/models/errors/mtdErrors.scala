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

package v1.models.errors

import play.api.libs.json.{Json, Writes}

case class MtdError(code: String, message: String)

object MtdError {
  implicit val writes: Writes[MtdError] = Json.writes[MtdError]
}

// Format Errors
object NinoFormatError extends MtdError("FORMAT_NINO", "The provided NINO is invalid")
object SubmissionIdFormatError extends MtdError("FORMAT_SUBMISSION_ID", "The provided submission ID is invalid")
object EmployerRefFormatError extends MtdError("FORMAT_EMPLOYER_REFERENCE", "The format of the Employer Reference number is invalid")
object TaxYearFormatError extends MtdError("FORMAT_TAX_YEAR", "The provided tax year is invalid")
object FromDateFormatError extends MtdError("FORMAT_FROM_DATE", "The provided From date is invalid")
object ToDateFormatError extends MtdError("FORMAT_TO_DATE", "The provided To date is invalid")
object DeductionFromDateFormatError extends MtdError("FORMAT_DEDUCTION_FROM_DATE", "The provided deductions From date is invalid")
object DeductionToDateFormatError extends MtdError("FORMAT_DEDUCTION_TO_DATE", "The provided deductions To date is invalid")


// Rule Errors
object RuleTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "Tax year not supported, because it precedes the earliest allowable tax year")

object RuleIncorrectOrEmptyBodyError extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted")

object RuleTaxYearRangeExceededError
    extends MtdError("RULE_TAX_YEAR_RANGE_EXCEEDED", "Tax year range exceeded. A tax year range of one year is required.")

object RuleDeductionsDateRangeInvalidError
    extends MtdError("RULE_DEDUCTIONS_DATE_RANGE_INVALID", "The specified date range is invalid")

object RuleDateRangeInvalidError
    extends MtdError("RULE_DATE_RANGE_INVALID", "The date range should be a valid tax year")

object RuleToDateBeforeFromDateError
    extends MtdError("RANGE_DEDUCTIONS_TO_DATE_BEFORE_DEDUCTIONS_FROM_DATE", "The deductions To date must be after the deductions From date")

object RuleSourceError
  extends MtdError("RULE_SOURCE_INVALID","The source is invalid" )

object RuleDeductionAmountError
    extends MtdError("RULE_DEDUCTIONS_AMOUNT", "The deductions amount should be a positive number less than 99999999999.99 up to 2 decimal places")

object RuleCostOfMaterialsError
    extends MtdError("RULE_COST_OF_MATERIALS", "The cost of materials should be a positive number less than 99999999999.99 up to 2 decimal places")

object RuleGrossAmountError
    extends MtdError("RULE_GROSS_AMOUNT_PAID", "The gross amount should be a positive number less than 99999999999.99 up to 2 decimal places")

object RuleMissingFromDateError
  extends MtdError("MISSING_FROM_DATE", "The From date parameter is missing")

object RuleMissingToDateError
  extends MtdError("MISSING_TO_DATE", "The To date parameter is missing")

object RuleFromDateError
  extends MtdError("FROM_DATE_NOT_IN_TAX_YEAR", "The from date is not the start of the tax year")

object RuleToDateError
  extends MtdError("TO_DATE_NOT_IN_TAX_YEAR", "The to date is not the end of the tax year")

object RuleNoChangeError
  extends MtdError("RULE_NO_CHANGE", "The deducted amount has not changed")

object RuleUnalignedDeductionPeriodError
  extends MtdError("RULE_UNALIGNED_DEDUCTIONS_PERIOD", "The deductions periods do not align with the tax year supplied")

object RuleDeductionDateRangeError
  extends MtdError("RULE_DEDUCTIONS_DATE_RANGE_INVALID", "The deductions period must align to the 6th of the month to the 5th of the tax year")

object RuleTaxYearNotEndedError
  extends MtdError("RULE_TAX_YEAR_NOT_ENDED", "The submission has been made before the tax year has ended")

object RuleDuplicateSubmissionError
  extends MtdError("RULE_DUPLICATE_SUBMISSION", "CIS deduction already exists for this tax year")

object RuleDuplicatePeriodError
  extends MtdError("RULE_DUPLICATE_PERIOD", "More than one deduction period has been supplied for the same month or period")

//Standard Errors
object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found")

object DownstreamError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")

object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request")

object BVRError extends MtdError("BUSINESS_ERROR", "Business validation error")

object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error")

//Authorisation Errors
object UnauthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised")
object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized")

// Accept header Errors
object  InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid")

object  UnsupportedVersionError extends MtdError("NOT_FOUND", "The requested resource could not be found")

object InvalidBodyTypeError extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body")
