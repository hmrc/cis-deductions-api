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

package api.models.errors

import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, INTERNAL_SERVER_ERROR, NOT_ACCEPTABLE, NOT_FOUND, UNAUTHORIZED, UNSUPPORTED_MEDIA_TYPE}

// MtdError types that are common across MTD APIs.

// Format Errors
object NinoFormatError   extends MtdError("FORMAT_NINO", "The provided NINO is invalid", BAD_REQUEST)
object StringFormatError extends MtdError("FORMAT_STRING", "The supplied string format is not valid", BAD_REQUEST)
object DateFormatError   extends MtdError("FORMAT_DATE", "The supplied date format is not valid", BAD_REQUEST)
object ValueFormatError  extends MtdError("FORMAT_VALUE", "The supplied value format is not valid", BAD_REQUEST)

object SubmissionIdFormatError extends MtdError("FORMAT_SUBMISSION_ID", "The provided submission ID is invalid", BAD_REQUEST)
object EmployerRefFormatError  extends MtdError("FORMAT_EMPLOYER_REFERENCE", "The format of the Employer Reference number is invalid", BAD_REQUEST)
object TaxYearFormatError      extends MtdError("FORMAT_TAX_YEAR", "The provided tax year is invalid", BAD_REQUEST)
object FromDateFormatError     extends MtdError("FORMAT_FROM_DATE", "The provided From date is invalid", BAD_REQUEST)
object ToDateFormatError       extends MtdError("FORMAT_TO_DATE", "The provided To date is invalid", BAD_REQUEST)
object DeductionFromDateFormatError extends MtdError("FORMAT_DEDUCTIONS_FROM_DATE", "The provided deductions From date is invalid", BAD_REQUEST)
object DeductionToDateFormatError   extends MtdError("FORMAT_DEDUCTIONS_TO_DATE", "The provided deductions To date is invalid", BAD_REQUEST)

// Rule Errors
object RuleTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "The tax year specified does not lie within the supported range", BAD_REQUEST)

object RuleIncorrectOrEmptyBodyError
    extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted", BAD_REQUEST)

object RuleTaxYearRangeExceededError
    extends MtdError("RULE_TAX_YEAR_RANGE_EXCEEDED", "Tax year range exceeded. A tax year range of one year is required.", BAD_REQUEST)

object RuleDeductionsDateRangeInvalidError
    extends MtdError(
      "RULE_DEDUCTIONS_DATE_RANGE_INVALID",
      "The deductions period must align from the 6th of one month to the 5th of the following month",
      BAD_REQUEST)

object RuleDateRangeInvalidError extends MtdError("RULE_DATE_RANGE_INVALID", "The date range should be a valid tax year", BAD_REQUEST)

object RuleToDateBeforeFromDateError
    extends MtdError(
      "RANGE_DEDUCTIONS_TO_DATE_BEFORE_DEDUCTIONS_FROM_DATE",
      "The deductions To date must be after the deductions From date",
      BAD_REQUEST)

object RuleSourceInvalidError extends MtdError("RULE_SOURCE_INVALID", "The source is invalid", BAD_REQUEST)

object RuleDeductionAmountError
    extends MtdError(
      "RULE_DEDUCTIONS_AMOUNT",
      "The deductions amount should be a positive number less than 99999999999.99 up to 2 decimal places",
      BAD_REQUEST)

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

object RuleMissingFromDateError extends MtdError("MISSING_FROM_DATE", "The From date parameter is missing", BAD_REQUEST)

object RuleMissingToDateError extends MtdError("MISSING_TO_DATE", "The To date parameter is missing", BAD_REQUEST)

object RuleUnalignedDeductionsPeriodError
    extends MtdError("RULE_UNALIGNED_DEDUCTIONS_PERIOD", "The deductions periods do not align with the tax year supplied", BAD_REQUEST)

object RuleTaxYearNotEndedError extends MtdError("RULE_TAX_YEAR_NOT_ENDED", "The submission has been made before the tax year has ended", BAD_REQUEST)

object RuleDuplicateSubmissionError extends MtdError("RULE_DUPLICATE_SUBMISSION", "CIS deduction already exists for this tax year", BAD_REQUEST)

object RuleDuplicatePeriodError
    extends MtdError("RULE_DUPLICATE_PERIOD", "More than one deduction period has been supplied for the same month or period", BAD_REQUEST)

object RuleDateRangeOutOfDate
    extends MtdError(
      "RULE_DATE_RANGE_OUT_OF_DATE",
      "The specified date range is outside the allowable tax years (the current tax year minus four years)",
      BAD_REQUEST)

object RuleTaxYearRangeInvalidError
    extends MtdError(
      "RULE_TAX_YEAR_RANGE_INVALID",
      "The specified tax year is outside the allowable tax years (the current tax year minus four years)",
      BAD_REQUEST)

object RuleTaxYearNotAligned
    extends MtdError("RULE_TAX_YEAR_NOT_ALIGNED", "The provided From date and To date parameters are not within the same tax year", BAD_REQUEST)

object InvalidTaxYearParameterError
    extends MtdError(
      code = "INVALID_TAX_YEAR_PARAMETER",
      message = "A tax year before 2023-24 was supplied",
      BAD_REQUEST
    )

//Standard Errors

object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found", NOT_FOUND)

object InternalError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred", INTERNAL_SERVER_ERROR)

object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request", BAD_REQUEST)

object BVRError extends MtdError("BUSINESS_ERROR", "Business validation error", BAD_REQUEST)

object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error", INTERNAL_SERVER_ERROR)

//Authorisation Errors
object ClientNotAuthorisedError    extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised", FORBIDDEN)
object ClientNotAuthenticatedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised", UNAUTHORIZED)
object InvalidBearerTokenError     extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized", UNAUTHORIZED)

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid", NOT_ACCEPTABLE)

object UnsupportedVersionError extends MtdError("NOT_FOUND", "The requested resource could not be found", NOT_FOUND)

object InvalidBodyTypeError extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)

//Stub errors
object RuleIncorrectGovTestScenarioError extends MtdError("RULE_INCORRECT_GOV_TEST_SCENARIO", "The Gov-Test-Scenario was not found", BAD_REQUEST)
