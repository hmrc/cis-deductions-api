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

package v1.services

import cats.data.EitherT
import cats.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.CreateConnector
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.create.CreateRequestData
import v1.models.response.create.CreateResponseModel
import v1.support.DownstreamResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateService @Inject() (connector: CreateConnector) extends DownstreamResponseMappingSupport with Logging {

  def createDeductions(request: CreateRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[CreateResponseModel]]] = {
    val result = for {
      desResponseWrapper <- EitherT(connector.create(request)).leftMap(mapDownstreamErrors(mappingDesToMtdError))
    } yield desResponseWrapper
    result.value
  }

  private def mappingDesToMtdError =
    Map(
      "INVALID_TAXABLE_ENTITY_ID"       -> NinoFormatError,
      "INVALID_PAYLOAD"                 -> RuleIncorrectOrEmptyBodyError,
      "INVALID_EMPREF"                  -> EmployerRefFormatError,
      "INVALID_REQUEST_TAX_YEAR_ALIGN"  -> RuleUnalignedDeductionsPeriodError,
      "INVALID_REQUEST_DATE_RANGE"      -> RuleDeductionsDateRangeInvalidError,
      "INVALID_REQUEST_BEFORE_TAX_YEAR" -> RuleTaxYearNotEndedError,
      "CONFLICT"                        -> RuleDuplicateSubmissionError,
      "INVALID_REQUEST_DUPLICATE_MONTH" -> RuleDuplicatePeriodError,
      "SERVER_ERROR"                    -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"             -> StandardDownstreamError,
      "INVALID_CORRELATIONID"           -> StandardDownstreamError
    )

}
