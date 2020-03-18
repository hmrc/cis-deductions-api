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

package v1.models.errors

import play.api.libs.json.{Json, Writes}
import v1.models.audit.AuditError

case class ErrorWrapper(correlationId: Option[String], errors: Seq[MtdError] = Seq()) {

  def auditErrors: Seq[AuditError] =
    errors.map(error => AuditError(error.code))
}

object ErrorWrapper {
  implicit val writes: Writes[ErrorWrapper] = (errorResponse: ErrorWrapper) => {

    val json = Json.obj(
      "code" -> errorResponse.errors.head.code,
      "message" -> errorResponse.errors.head.message
    )

    if(errorResponse.errors.length > 1){
      json + ("errors" -> Json.toJson(errorResponse.errors.tail))
    } else {
      json
    }
  }
}
