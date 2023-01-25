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

import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.auth.UserDetails
import api.models.errors.ErrorWrapper
import api.services.AuditService
import cats.syntax.either._
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext

object AuditHandler {

  def apply(auditService: AuditService,
            auditType: String,
            transactionName: String,
            pathParams: Map[String, String],
            queryParams: Option[Map[String, Option[String]]] = None,
            requestBody: Option[JsValue] = None,
            includeResponse: Boolean = false): AuditHandler = new AuditHandler(
    auditService = auditService,
    auditType = auditType,
    transactionName = transactionName,
    pathParams = pathParams,
    queryParams = queryParams,
    requestBody = requestBody,
    responseBodyMap = if (includeResponse) identity else _ => None
  )

}

case class AuditHandler(auditService: AuditService,
                        auditType: String,
                        transactionName: String,
                        pathParams: Map[String, String],
                        queryParams: Option[Map[String, Option[String]]],
                        requestBody: Option[JsValue],
                        responseBodyMap: Option[JsValue] => Option[JsValue])
    extends RequestContextImplicits {

  def performAudit(userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]])(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Unit = {
    val auditEvent = {
      val auditResponse = AuditResponse(httpStatus, response.map(responseBodyMap).leftMap(ew => ew.auditErrors))

      val detail = GenericAuditDetail(
        userDetails = userDetails,
        pathParams = pathParams,
        queryParams = queryParams,
        requestBody = requestBody,
        `X-CorrelationId` = ctx.correlationId,
        auditResponse = auditResponse
      )

      AuditEvent(auditType, transactionName, detail)
    }

    auditService.auditEvent(auditEvent)
  }

}
