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

package v3.services

import cats.implicits.toBifunctorOps
import models.errors.SubmissionIdFormatError
import shared.controllers.RequestContext
import shared.models.errors.{InternalError, MtdError, NinoFormatError, NotFoundError, RuleTaxYearNotSupportedError, TaxYearFormatError}
import shared.services.{BaseService, ServiceOutcome}
import v3.connectors.DeleteConnector
import v3.models.request.delete.DeleteRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteService @Inject() (connector: DeleteConnector) extends BaseService {

  def deleteDeductions(request: DeleteRequestData)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {

    connector.delete(request).map(_.leftMap(mapDownstreamErrors(errorMap)))

  }

  private val errorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_SUBMISSION_ID"     -> SubmissionIdFormatError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_TAX_YEAR"       -> TaxYearFormatError,
      "INVALID_SUBMISSIONID"   -> SubmissionIdFormatError,
      "NOT_FOUND"              -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors

  }

}
