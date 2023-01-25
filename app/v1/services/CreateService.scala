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
import v1.connectors.CreateConnector
import v1.models.request.create.CreateRequestData
import v1.models.response.create.CreateResponseModel

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateService @Inject() (connector: CreateConnector) extends BaseService {

  def createDeductions(request: CreateRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[Either[ErrorWrapper, ResponseWrapper[CreateResponseModel]]] = {

    connector.create(request).map(_.leftMap(mapDownstreamErrors(errorMap)))
  }

  private val errorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID"       -> NinoFormatError,
      "INVALID_PAYLOAD"                 -> RuleIncorrectOrEmptyBodyError,
      "INVALID_EMPREF"                  -> EmployerRefFormatError,
      "INVALID_REQUEST_TAX_YEAR_ALIGN"  -> RuleUnalignedDeductionsPeriodError,
      "INVALID_REQUEST_DATE_RANGE"      -> RuleDeductionsDateRangeInvalidError,
      "INVALID_REQUEST_BEFORE_TAX_YEAR" -> RuleTaxYearNotEndedError,
      "CONFLICT"                        -> RuleDuplicateSubmissionError,
      "INVALID_REQUEST_DUPLICATE_MONTH" -> RuleDuplicatePeriodError,
      "SERVER_ERROR"                    -> InternalError,
      "SERVICE_UNAVAILABLE"             -> InternalError,
      "INVALID_CORRELATIONID"           -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_CORRELATION_ID" -> InternalError,
      "INVALID_TAX_YEAR"       -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
      "INVALID_TAX_YEAR_ALIGN" -> RuleUnalignedDeductionsPeriodError,
      "INVALID_DATE_RANGE"     -> RuleDeductionsDateRangeInvalidError,
      "DUPLICATE_MONTH"        -> RuleDuplicatePeriodError,
      "EARLY_SUBMISSION"       -> RuleTaxYearNotEndedError
    )

    errors ++ extraTysErrors
  }

}
