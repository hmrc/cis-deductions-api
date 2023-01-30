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

package api.models.audit

import api.controllers.{AuditHandler, RequestContext}
import api.models.auth.UserDetails
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsValue, OWrites}

case class GenericAuditDetail(userType: String,
                              agentReferenceNumber: Option[String],
                              params: Map[String, String],
                              requestBody: Option[JsValue],
                              `X-CorrelationId`: String,
                              auditResponse: AuditResponse)

object GenericAuditDetail {

  implicit val writes: OWrites[GenericAuditDetail] = (
    (JsPath \ "userType").write[String] and
      (JsPath \ "agentReferenceNumber").writeNullable[String] and
      JsPath.write[Map[String, String]] and
      (JsPath \ "request").writeNullable[JsValue] and
      (JsPath \ "X-CorrelationId").write[String] and
      (JsPath \ "response").write[AuditResponse]
  )(unlift(GenericAuditDetail.unapply))

  def apply(userDetails: UserDetails,
            params: Map[String, String],
            requestBody: Option[JsValue],
            `X-CorrelationId`: String,
            auditResponse: AuditResponse): GenericAuditDetail = {

    GenericAuditDetail(
      userType = userDetails.userType,
      agentReferenceNumber = userDetails.agentReferenceNumber,
      params = params,
      requestBody = requestBody,
      `X-CorrelationId` = `X-CorrelationId`,
      auditResponse = auditResponse
    )
  }

  def auditDetailCreator(params: Map[String, String]): AuditHandler.AuditDetailCreator[GenericAuditDetail] =
    new AuditHandler.AuditDetailCreator[GenericAuditDetail] {

      def createAuditDetail(userDetails: UserDetails, requestBody: Option[JsValue], auditResponse: AuditResponse)(implicit
          ctx: RequestContext): GenericAuditDetail =
        GenericAuditDetail(
          userDetails = userDetails,
          params = params,
          requestBody = requestBody,
          `X-CorrelationId` = ctx.correlationId,
          auditResponse = auditResponse
        )

    }

}
