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

import api.services.{BaseService, ServiceOutcome}
import cats.implicits.toBifunctorOps
import shared.controllers.RequestContext
import shared.models.errors._
import v1.connectors.RetrieveConnector
import v1.models.request.retrieve.RetrieveRequestData
import v1.models.response.retrieve.{CisDeductions, RetrieveResponseModel}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveService @Inject() (connector: RetrieveConnector) extends BaseService {

  def retrieveDeductions(request: RetrieveRequestData)(implicit
                                                       ctx: RequestContext,
                                                       ec: ExecutionContext): Future[ServiceOutcome[RetrieveResponseModel[CisDeductions]]] = {

    val errorMapping = if (request.taxYear.useTaxYearSpecificApi) errorMapTys else errorMap

    connector.retrieve(request).map(_.leftMap(mapDownstreamErrors(errorMapping)))

  }

  private val errorMap: Map[String, MtdError] =
    Map(
      "INVALID_DATE_RANGE"        -> RuleDateRangeOutOfDateError,
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "INVALID_PERIOD_START"      -> FromDateFormatError,
      "INVALID_PERIOD_END"        -> ToDateFormatError,
      "INVALID_SOURCE"            -> RuleSourceInvalidError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

  private val errorMapTys: Map[String, MtdError] =
    errorMap ++ Map(
      "INVALID_DATE_RANGE"     -> RuleTaxYearRangeInvalidError,
      "INVALID_TAX_YEAR"       -> InternalError,
      "INVALID_START_DATE"     -> FromDateFormatError,
      "INVALID_END_DATE"       -> ToDateFormatError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
      "INVALID_CORRELATIONID"  -> InternalError
    )

}
