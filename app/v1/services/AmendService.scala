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

package v1.services

import api.controllers.RequestContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.BaseService
import cats.implicits.toBifunctorOps
import v1.connectors.AmendConnector
import v1.models.request.amend.AmendRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendService @Inject() (connector: AmendConnector) extends BaseService {

  def amendDeductions(
      request: AmendRequestData)(implicit ctx: RequestContext, ec: ExecutionContext): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {

    connector.amendDeduction(request).map(_.leftMap(mapDownstreamErrors(errorMap)))

  }

  private val errorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_SUBMISSION_ID"     -> SubmissionIdFormatError,
      "INVALID_CORRELATIONID"     -> StandardDownstreamError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "INVALID_TAX_YEAR_ALIGN"    -> RuleUnalignedDeductionsPeriodError,
      "INVALID_DATE_RANGE"        -> RuleDeductionsDateRangeInvalidError,
      "INVALID_PAYLOAD"           -> RuleIncorrectOrEmptyBodyError,
      "DUPLICATE_MONTH"           -> RuleDuplicatePeriodError,
      "SERVICE_UNAVAILABLE"       -> StandardDownstreamError,
      "SERVICE_ERROR"             -> StandardDownstreamError
    )
    val extraTysErrors = Map(
      "INVALID_TAX_YEAR"       -> StandardDownstreamError,
      "INVALID_CORRELATION_ID" -> StandardDownstreamError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
