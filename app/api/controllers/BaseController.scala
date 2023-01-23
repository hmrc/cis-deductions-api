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

package api.controllers

import api.models.audit
import api.models.audit.{AuditResponse, GenericAuditDetail}
import api.models.auth.UserDetails
import api.models.errors.{ErrorWrapper, StandardDownstreamError}
import api.models.request.RawData
import play.api.http.Status
import play.api.libs.json.JsValue
import utils.Logging

trait BaseController {
  self: Logging =>

  def createAuditDetails[A <: RawData](rawData: A,
                                       statusCode: Int,
                                       correlationId: String,
                                       userDetails: UserDetails,
                                       submissionId: Option[String],
                                       errorWrapper: Option[ErrorWrapper],
                                       requestBody: Option[JsValue] = None,
                                       responseBody: Option[JsValue] = None): GenericAuditDetail = {
    val response = errorWrapper
      .map { wrapper =>
        audit.AuditResponse(statusCode, Some(wrapper.auditErrors), None)
      } match {
      case Some(wrapper) => wrapper
      case None =>
        statusCode match {
          case Status.NO_CONTENT => AuditResponse(statusCode, None, None)
          case _                 => AuditResponse(statusCode, None, responseBody)
        }
    }

    audit.GenericAuditDetail(userDetails.userType, userDetails.agentReferenceNumber, rawData.nino, submissionId, correlationId, requestBody, response)
  }

}
