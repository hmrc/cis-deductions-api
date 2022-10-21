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
import v1.connectors.RetrieveConnector
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieve.RetrieveRequestData
import v1.models.response.retrieve.{CisDeductions, RetrieveResponseModel}
import v1.support.DownstreamResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveService @Inject() (connector: RetrieveConnector) extends DownstreamResponseMappingSupport with Logging {

  def retrieveDeductions(request: RetrieveRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveResponseModel[CisDeductions]]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieve(request)).leftMap(mapDownstreamErrors(mappingDesToMtdError))
    } yield desResponseWrapper
    result.value
  }

  private def mappingDesToMtdError: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "INVALID_DATE_RANGE"        -> RuleDateRangeOutOfDate,
      "INVALID_PERIOD_START"      -> FromDateFormatError,
      "INVALID_PERIOD_END"        -> ToDateFormatError,
      "INVALID_SOURCE"            -> RuleSourceError,
      "SERVER_ERROR"              -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"       -> StandardDownstreamError
    )

    val extraTysErrors = Map(
      "INVALID_TAX_YEAR"       -> StandardDownstreamError,
      "INVALID_START_DATE"     -> FromDateFormatError,
      "INVALID_END_DATE"       -> ToDateFormatError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
      "INVALID_CORRELATIONID"  -> StandardDownstreamError
    )

    errors ++ extraTysErrors
  }

}
