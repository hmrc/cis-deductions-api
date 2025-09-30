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

package definition

import shared.config.SharedAppConfig
import shared.definition.*
import shared.routing.{Version2, Version3}
import shared.utils.Logging

import javax.inject.{Inject, Singleton}

@Singleton
class CisApiDefinitionFactory @Inject() (protected val appConfig: SharedAppConfig) extends ApiDefinitionFactory with Logging {

  val definition: Definition =
    Definition(
      api = APIDefinition(
        name = "CIS Deductions (MTD)",
        description = "An API for providing Construction industry scheme data",
        context = appConfig.apiGatewayContext,
        categories = Seq(mtdCategory),
        versions = List(
          APIVersion(
            version = Version2,
            status = buildAPIStatus(Version2),
            endpointsEnabled = appConfig.endpointsEnabled(Version2)
          ),
          APIVersion(
            version = Version3,
            status = buildAPIStatus(Version3),
            endpointsEnabled = appConfig.endpointsEnabled(Version3)
          )
        ),
        requiresTrust = None
      )
    )

}
