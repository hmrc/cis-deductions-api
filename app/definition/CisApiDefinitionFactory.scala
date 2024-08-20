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

package definition

import shared.config.AppConfig
import shared.definition._
import shared.routing.{Version1, Version2}
import shared.utils.Logging

import javax.inject.{Inject, Singleton}

@Singleton
class CisApiDefinitionFactory @Inject() (protected val appConfig: AppConfig) extends ApiDefinitionFactory with Logging {

  lazy val definition: Definition =
    Definition(
      scopes = scopes,
      api = APIDefinition(
        name = "CIS Deductions (MTD)",
        description = "An API for providing Construction industry scheme data",
        context = appConfig.apiGatewayContext,
        categories = Seq(mtdCategory),
        versions = Seq(
          APIVersion(
            version = Version1,
            status = buildAPIStatus(Version1),
            endpointsEnabled = appConfig.endpointsEnabled(Version1)
          ),
          APIVersion(
            version = Version2,
            status = buildAPIStatus(Version2),
            endpointsEnabled = appConfig.endpointsEnabled(Version2)
          )
        ),
        requiresTrust = None
      )
    )

}
