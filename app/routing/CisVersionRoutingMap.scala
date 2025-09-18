/*
 * Copyright 2025 HM Revenue & Customs
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

package routing

import play.api.routing.Router
import shared.routing.{Version, Version2, Version3, VersionRoutingMap}

import javax.inject.Inject

case class CisVersionRoutingMap @Inject() (
    defaultRouter: Router,
    v2Router: v2.Routes,
    v3Router: v3.Routes
) extends VersionRoutingMap {

  val map: Map[Version, Router] = Map(
    Version2 -> v2Router,
    Version3 -> v3Router
  )

}
