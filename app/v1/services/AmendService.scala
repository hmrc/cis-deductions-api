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

package v1.services

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.AmendConnector
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.AmendRequestData
import v1.models.responseData.AmendResponse
import v1.support.DesResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendService @Inject()(connector: AmendConnector) extends DesResponseMappingSupport with Logging {

  def amendDeductions(request: AmendRequestData)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext): Future[Either[ErrorWrapper, ResponseWrapper[AmendResponse]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.amendDeduction(request)).leftMap(mapDesErrors(desErrorMap))
    } yield desResponseWrapper.map(des => des)

    result.value
  }

  private def desErrorMap =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "FORMAT_DEDUCTION_FROM_DATE" -> DeductionFromDateFormatError,
      "FORMAT_DEDUCTION_TO_DATE" -> DeductionToDateFormatError,
      "FORMAT_FROM_DATE" -> FromDateFormatError,
      "FORMAT_TO_DATE" -> ToDateFormatError,
      "RULE_DEDUCTIONS_DATE_RANGE_INVALID" -> RuleDeductionsDateRangeInvalidError,
      "RANGE_DEDUCTIONS_TO_DATE_BEFORE_DEDUCTIONS_FROM_DATE" -> RuleToDateBeforeFromDateError,
      "NOT_FOUND" -> NotFoundError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )
}