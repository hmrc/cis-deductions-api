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

package api.models.hateoas

import play.api.libs.json.{JsObject, Json, OWrites, Writes}

object HateoasWrapper {

  implicit def writes[Payload: OWrites]: Writes[HateoasWrapper[Payload]] = Writes { wrapper =>
    // Explicitly use writes method rather than Json.toJson so that we don't have to
    // throw out meaningless JsArray, JsString, etc cases...
    implicitly[OWrites[Payload]].writes(wrapper.payload) match {
      case payloadJson: JsObject =>
        if (wrapper.links.nonEmpty) {
          // Manually construct JsObject circumventing `.+` operator to preserve order of fields
          JsObject(payloadJson.fields :+ "links" -> Json.toJson(wrapper.links))
        } else {
          payloadJson
        }
    }
  }

}

case class HateoasWrapper[Payload](payload: Payload, links: Seq[Link])
