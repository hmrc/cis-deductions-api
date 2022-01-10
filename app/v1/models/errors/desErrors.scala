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

package v1.models.errors

import play.api.libs.json.{Json, Reads}

case class DesErrorCode(code: String) {
  def toMtd: MtdError = MtdError(code = code, message = "")
}

object DesErrorCode {
  implicit val reads: Reads[DesErrorCode] = Json.reads[DesErrorCode]
}

sealed trait DesError

case class DesErrors(errors: Seq[DesErrorCode]) extends DesError

object DesErrors {
  def single(error: DesErrorCode): DesErrors = DesErrors(Seq(error))
}

case class OutboundError(error: MtdError, errors: Option[Seq[MtdError]] = None) extends DesError
