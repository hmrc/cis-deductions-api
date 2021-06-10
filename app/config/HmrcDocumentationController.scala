/*
 * Copyright 2021 HM Revenue & Customs
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

package config

import controllers.Assets
import play.api.http.HttpErrorHandler
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

/* TODO: This file exists as a placeholder for uk.gov.hmrc.api.controllers.DocumentationController
    until the play-hmrc-api library gets upgraded to support Play 27*/

class HmrcDocumentationController(cc: ControllerComponents,
                                  assets: Assets,
                                  errorHandler: HttpErrorHandler) extends BackendController(cc) {

  def documentation(version: String, endpointName: String): Action[AnyContent] =
    assets.at(s"/public/api/documentation/$version", s"${endpointName.replaceAll(" ", "-")}.xml")

  def definition(): Action[AnyContent] =
    assets.at(s"/public/api", "definition.json")

  def conf(version: String, file: String): Action[AnyContent] =
    assets.at(s"/public/api/conf/$version", file)
}
