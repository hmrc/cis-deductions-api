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

package v1.controllers

import play.api.libs.json.JsValue
import play.api.mvc.Result
import utils.Logging
import v1.models.audit.{AuditResponse, GenericAuditDetail}
import v1.models.auth.UserDetails
import v1.models.errors.ErrorWrapper
import v1.models.request.RawData
import play.api.http.Status

trait BaseController {
  self: Logging =>

  implicit class Response(result: Result) {

    def withApiHeaders(correlationId: String, responseHeaders: (String, String)*): Result = {

      val newHeaders: Seq[(String, String)] = responseHeaders ++ Seq(
        "X-CorrelationId"        -> correlationId,
        "X-Content-Type-Options" -> "nosniff",
        "Content-Type"           -> "application/json"
      )

      result.copy(header = result.header.copy(headers = result.header.headers ++ newHeaders))
    }

  }

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
        AuditResponse(statusCode, Some(wrapper.auditErrors), None)
      } match {
      case Some(wrapper) => wrapper
      case None =>
        statusCode match {
          case Status.NO_CONTENT => AuditResponse(statusCode, None, None)
          case _                 => AuditResponse(statusCode, None, responseBody)
        }
    }

    GenericAuditDetail(userDetails.userType, userDetails.agentReferenceNumber, rawData.nino, submissionId, correlationId, requestBody, response)
  }

}
