/*
 * Copyright 2026 HM Revenue & Customs
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

package api.definition

import api.config.AppConfig
import api.definition.APIAccessType.{CONTROLLED, PUBLIC}
import api.routing.{Version, Version3}
import api.utils.Logging
import cats.data.Validated.Invalid

import javax.inject.{Inject, Singleton}

@Singleton
class ApiDefinitionFactory @Inject() (appConfig: AppConfig) extends Logging {

  protected val mtdCategory = "INCOME_TAX_MTD"

  lazy val definition: Definition =
    Definition(
      api = APIDefinition(
        name = "CIS Deductions (MTD)",
        description = "An API for providing Construction industry scheme data",
        context = appConfig.apiGatewayContext,
        categories = Seq(mtdCategory),
        versions = List(
          APIVersion(
            version = Version3,
            status = buildAPIStatus(Version3),
            access = if (appConfig.controlledAccessEnabled) CONTROLLED else PUBLIC,
            endpointsEnabled = appConfig.endpointsEnabled(Version3)
          )
        ),
        requiresTrust = None
      )
    )

  def buildAPIStatus(version: Version): APIStatus = {
    checkDeprecationConfigFor(version)

    APIStatus.parser
      .lift(appConfig.apiStatus(version))
      .getOrElse {
        logger.error("[ApiDefinition][buildApiStatus] no API Status found in config. Reverting to Alpha")
        APIStatus.ALPHA
      }
  }

  private def checkDeprecationConfigFor(version: Version): Unit =
    appConfig.deprecationFor(version) match {
      case Invalid(error) => throw new Exception(error)
      case _              => ()
    }

}
