/*
 * Copyright 2024 HM Revenue & Customs
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

package shared.schema

import play.api.libs.json.Reads

trait DownstreamReadable[Base] {

  /** This is the type of response returned by the connector.
    *
    * It is not necessarily the same as the response type returned by the service to the controller.
    */
  type DownstreamResp <: Base

  implicit def connectorReads: Reads[DownstreamResp]
}
