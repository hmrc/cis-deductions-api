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

package api.models.errors

import api.models.domain.DateRange
import play.api.http.Status.*

// MtdError types that are common across MTD APIs.

// Format Errors
object NinoFormatError extends MtdError("FORMAT_NINO", "The NINO format is invalid", BAD_REQUEST)

object TaxYearFormatError extends MtdError("FORMAT_TAX_YEAR", "The taxYear format is invalid", BAD_REQUEST)

object ToDateFormatError extends MtdError(code = "FORMAT_TO_DATE", message = "The toDate format is invalid", BAD_REQUEST)

object FromDateFormatError extends MtdError(code = "FORMAT_FROM_DATE", message = "The fromDate format is invalid", BAD_REQUEST)

object StartDateFormatError extends MtdError("FORMAT_START_DATE", "The provided Start date is invalid", BAD_REQUEST)

object EndDateFormatError extends MtdError("FORMAT_END_DATE", "The provided End date is invalid", BAD_REQUEST)

object DateFormatError extends MtdError(code = "FORMAT_DATE", message = "The supplied date format is not valid", BAD_REQUEST)

object ValueFormatError extends MtdError("FORMAT_VALUE", "The value must be between 0 and 99999999999.99", BAD_REQUEST) {

  def forPathAndRange(path: String, min: String, max: String): MtdError =
    ValueFormatError.copy(paths = Some(Seq(path)), message = s"The value must be between $min and $max")

}

//Standard Errors
object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found", NOT_FOUND)

object InternalError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred", INTERNAL_SERVER_ERROR)

object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request", BAD_REQUEST)

object GatewayTimeoutError extends MtdError("GATEWAY_TIMEOUT", "The request has timed out", GATEWAY_TIMEOUT)

object BVRError extends MtdError("BUSINESS_ERROR", "Business validation error", BAD_REQUEST)

object InvalidHttpMethodError extends MtdError("INVALID_HTTP_METHOD", "Invalid HTTP method", METHOD_NOT_ALLOWED)

object InvalidBodyTypeError extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)

//Authentication/Authorisation errors

/** Authentication OK but not allowed access to the requested resource
  */
object ClientOrAgentNotAuthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client or agent is not authorised", FORBIDDEN) {
  def withStatus401: MtdError = copy(httpStatus = UNAUTHORIZED)
}

object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized", UNAUTHORIZED)

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid", NOT_ACCEPTABLE)

object UnsupportedVersionError extends MtdError("NOT_FOUND", "The requested resource could not be found", NOT_FOUND)

// Common rule errors

object RuleTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "The tax year specified does not lie within the supported range", BAD_REQUEST) {

  def dateRangeMsg: MtdError =
    RuleTaxYearNotSupportedError.copy(message = "The specified tax year is outside the allowable tax years (the current tax year minus four years)")

}

object RuleIncorrectOrEmptyBodyError
    extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted", BAD_REQUEST)

object RuleTaxYearRangeInvalidError extends MtdError("RULE_TAX_YEAR_RANGE_INVALID", "A tax year range of one year is required", BAD_REQUEST)

object RuleTaxYearNotEndedError extends MtdError("RULE_TAX_YEAR_NOT_ENDED", "The specified tax year has not yet ended", BAD_REQUEST)

object RuleDateRangeInvalidError extends MtdError(code = "RULE_DATE_RANGE_INVALID", message = "The specified date range is invalid", BAD_REQUEST)

object RuleEndBeforeStartDateError
    extends MtdError("RULE_END_DATE_BEFORE_START_DATE", "The supplied accounting period end date is before the start date", BAD_REQUEST)

//Stub Errors
object RuleIncorrectGovTestScenarioError
    extends MtdError("RULE_INCORRECT_GOV_TEST_SCENARIO", "The supplied Gov-Test-Scenario is not valid", BAD_REQUEST)

object SubmissionIdFormatError extends MtdError("FORMAT_SUBMISSION_ID", "The provided submission ID is invalid", BAD_REQUEST)

object RuleUnalignedDeductionsPeriodError
    extends MtdError("RULE_UNALIGNED_DEDUCTIONS_PERIOD", "The deductions periods do not align with the tax year supplied", BAD_REQUEST)

object RuleDuplicatePeriodError
    extends MtdError("RULE_DUPLICATE_PERIOD", "More than one deduction period has been supplied for the same month or period", BAD_REQUEST) {

  def forDuplicatedPeriod(dateRange: DateRange, paths: Seq[String]): MtdError = copy(
    message = s"More than one deduction period has been supplied for the period '${dateRange.startDate}' to '${dateRange.endDate}''",
    paths = Some(paths)
  )

}

object RuleDeductionsDateRangeInvalidError
    extends MtdError(
      "RULE_DEDUCTIONS_DATE_RANGE_INVALID",
      "The deductions period must align from the 6th of one month to the 5th of the following month",
      BAD_REQUEST)

object RuleSourceInvalidError extends MtdError("RULE_SOURCE_INVALID", "The source is invalid", BAD_REQUEST)

object RuleDuplicateSubmissionError
    extends MtdError("RULE_DUPLICATE_SUBMISSION", "CIS deduction already exists for contractor this tax year", BAD_REQUEST)

object EmployerRefFormatError extends MtdError("FORMAT_EMPLOYER_REFERENCE", "The format of the Employer Reference number is invalid", BAD_REQUEST)

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

object RuleOutsideAmendmentWindowError
    extends MtdError(
      code = "RULE_OUTSIDE_AMENDMENT_WINDOW",
      message = "You are outside the amendment window",
      BAD_REQUEST
    )

object ContractorNameFormatError
    extends MtdError(
      code = "FORMAT_CONTRACTOR_NAME ",
      message = "The provided contractor name is invalid",
      BAD_REQUEST
    )
