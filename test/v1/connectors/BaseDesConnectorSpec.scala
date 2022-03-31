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

package v1.connectors

import config.AppConfig
import mocks.MockAppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper

import scala.concurrent.Future

class BaseDesConnectorSpec extends ConnectorSpec {

  // WLOG
  case class Result(value: Int)

  // WLOG
  val body = "body"

  val outcome = Right(ResponseWrapper(correlationId, Result(2)))

  val url         = "some/url?param=value"
  val absoluteUrl = s"$baseUrl/$url"

  implicit val httpReads: HttpReads[DesOutcome[Result]] = mock[HttpReads[DesOutcome[Result]]]

  class Test(desEnvironmentHeaders: Option[Seq[String]]) extends MockHttpClient with MockAppConfig {

    val connector: BaseDesConnector = new BaseDesConnector {
      val http: HttpClient     = mockHttpClient
      val appConfig: AppConfig = mockAppConfig
    }

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns desEnvironmentHeaders
  }

  "BaseDesConnector" when {
    val requiredHeaders: Seq[(String, String)] = Seq(
      "Environment"       -> "des-environment",
      "Authorization"     -> s"Bearer des-token",
      "User-Agent"        -> "individual-cis-deductions-api",
      "CorrelationId"     -> correlationId,
      "Gov-Test-Scenario" -> "DEFAULT"
    )

    val excludedHeaders: Seq[(String, String)] = Seq(
      "AnotherHeader" -> "HeaderValue"
    )

    "making a HTTP request to a downstream service (i.e DES)" must {
      testHttpMethods(dummyDesHeaderCarrierConfig, requiredHeaders, excludedHeaders, Some(allowedDesHeaders))

      "exclude all `otherHeaders` when no external service header allow-list is found" should {
        val requiredHeaders: Seq[(String, String)] = Seq(
          "Environment"   -> "des-environment",
          "Authorization" -> s"Bearer des-token",
          "User-Agent"    -> "individual-cis-deductions-api",
          "CorrelationId" -> correlationId
        )

        testHttpMethods(dummyDesHeaderCarrierConfig, requiredHeaders, otherHeaders, None)
      }
    }
  }

  def testHttpMethods(config: HeaderCarrier.Config,
                      requiredHeaders: Seq[(String, String)],
                      excludedHeaders: Seq[(String, String)],
                      desEnvironmentHeaders: Option[Seq[String]]): Unit = {

    "complete the request successfully with the required headers" when {
      "GET" in new Test(desEnvironmentHeaders) {
        MockedHttpClient
          .get(absoluteUrl, config, requiredHeaders, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.get(DesUri[Result](url))) shouldBe outcome
      }

      "POST" in new Test(desEnvironmentHeaders) {
        implicit val hc: HeaderCarrier                 = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredHeadersPost: Seq[(String, String)] = requiredHeaders ++ Seq("Content-Type" -> "application/json")

        MockedHttpClient
          .post(absoluteUrl, config, body, requiredHeadersPost, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.post(body, DesUri[Result](url))) shouldBe outcome
      }
    }
  }

}
