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

package v1.models.audit

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsValue, OWrites}
import v1.models.auth.UserDetails

case class GenericAuditDetail(userType: String,
                              agentReferenceNumber: Option[String],
                              params: Map[String, String],
                              requestBody: Option[JsValue],
                              `X-CorrelationId`: String,
                              auditResponse: CreateAuditResponse)

object GenericAuditDetail {

  implicit val writes: OWrites[GenericAuditDetail] = (
    (JsPath \ "userType").write[String] and
      (JsPath \ "agentReferenceNumber").writeNullable[String] and
      JsPath.write[Map[String, String]] and
      (JsPath \ "request").writeNullable[JsValue] and
      (JsPath \ "X-CorrelationId").write[String] and
      (JsPath \ "response").write[CreateAuditResponse]
    ) (unlift(GenericAuditDetail.unapply))

  def apply(userDetails: UserDetails,
            params: Map[String, String],
            requestBody: Option[JsValue],
            `X-CorrelationId`: String,
            auditResponse: CreateAuditResponse): GenericAuditDetail = {

    GenericAuditDetail(
      userType = userDetails.userType,
      agentReferenceNumber = userDetails.agentReferenceNumber,
      params = params,
      requestBody = requestBody,
      `X-CorrelationId` = `X-CorrelationId`,
      auditResponse = auditResponse
    )
  }
}
