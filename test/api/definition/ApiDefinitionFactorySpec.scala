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

import api.config.Deprecation.NotDeprecated
import api.config.MockAppConfig
import api.definition.APIStatus.{ALPHA, BETA}
import api.mocks.MockHttpClient
import api.routing.*
import api.utils.UnitSpec
import cats.implicits.catsSyntaxValidatedId
import definition.CisApiDefinitionFactory

import scala.language.reflectiveCalls

class ApiDefinitionFactorySpec extends UnitSpec with MockHttpClient with MockAppConfig {

  "definition" when {
    "called" should {
      "return a valid Definition case class" in new Test {
        List(Version3).foreach { version =>
          MockedAppConfig.apiStatus(version) returns "BETA"
          MockedAppConfig.endpointsEnabled(version).returns(true).anyNumberOfTimes()
          MockedAppConfig.controlledAccessEnabled returns false
          MockedAppConfig.deprecationFor(version).returns(NotDeprecated.valid).anyNumberOfTimes()
          MockedAppConfig.apiGatewayContext returns "individuals/deductions/cis"
        }

        apiDefinitionFactory.definition shouldBe
          Definition(
            api = APIDefinition(
              name = "CIS Deductions (MTD)",
              description = "An API for providing Construction industry scheme data",
              context = "individuals/deductions/cis",
              categories = List("INCOME_TAX_MTD"),
              versions = List(
                APIVersion(
                  Version3,
                  status = BETA,
                  access = APIAccessType.PUBLIC,
                  endpointsEnabled = true
                )
              ),
              requiresTrust = None
            )
          )
      }
    }
  }

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {

      s"return the expected status" in new Test {
        MockedAppConfig.deprecationFor(Version3).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedAppConfig.apiStatus(Version3) returns "BETA"

        apiDefinitionFactory.buildAPIStatus(Version3) shouldBe BETA
      }

    }

    "the 'apiStatus' parameter is present but invalid" should {
      s"default to alpha" in new Test {
        MockedAppConfig.deprecationFor(Version3).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedAppConfig.apiStatus(Version3) returns "not-a-status"

        apiDefinitionFactory.buildAPIStatus(Version3) shouldBe ALPHA
      }
    }

    "the 'deprecatedOn' parameter is missing for a deprecated version" should {
      "throw an exception" in new Test {
        MockedAppConfig
          .deprecationFor(Version3)
          .returns("deprecatedOn date is required for a deprecated version".invalid)
          .anyNumberOfTimes()

        val exception: Exception = intercept[Exception] {
          apiDefinitionFactory.buildAPIStatus(Version3)
        }

        val exceptionMessage: String = exception.getMessage
        exceptionMessage shouldBe "deprecatedOn date is required for a deprecated version"
      }
    }
  }

  "set the access level" when {
    "the controlled access flag is enabled" should {
      "to be CONTROLLED" in new Test {
        MockedAppConfig.endpointsEnabled(Version3).returns(true).anyNumberOfTimes()
        MockedAppConfig.apiStatus(Version3) returns "BETA"
        MockedAppConfig.deprecationFor(Version3).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedAppConfig.apiGatewayContext returns "individuals/deductions/cis"
        MockedAppConfig.controlledAccessEnabled returns true

        apiDefinitionFactory.definition.api.versions.head.access shouldBe APIAccessType.CONTROLLED
      }
    }

    "the controlled access flag is disabled" should {
      "return PUBLIC" in new Test {
        MockedAppConfig.endpointsEnabled(Version3).returns(true).anyNumberOfTimes()
        MockedAppConfig.apiStatus(Version3) returns "BETA"
        MockedAppConfig.deprecationFor(Version3).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedAppConfig.apiGatewayContext returns "individuals/deductions/cis"
        MockedAppConfig.controlledAccessEnabled returns false

        apiDefinitionFactory.definition.api.versions.head.access shouldBe APIAccessType.PUBLIC
      }
    }
  }

  class Test {
    val apiDefinitionFactory: ApiDefinitionFactory = new CisApiDefinitionFactory(mockAppConfig)
  }

}
