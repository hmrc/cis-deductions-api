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

package v1.connectors

import mocks.MockAppConfig
import org.scalamock.handlers.CallHandler
import play.api.Configuration
import play.api.http.{HeaderNames, MimeTypes, Status}
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.MockHttpClient

import scala.concurrent.{ExecutionContext, Future}

trait ConnectorSpec extends UnitSpec with Status with MimeTypes with HeaderNames {

  lazy val baseUrl = "http://test-BaseUrl"

  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  implicit val hc: HeaderCarrier     = HeaderCarrier()
  implicit val ec: ExecutionContext  = scala.concurrent.ExecutionContext.global

  val otherHeaders: Seq[(String, String)] = Seq(
    "Gov-Test-Scenario" -> "DEFAULT",
    "AnotherHeader"     -> "HeaderValue"
  )

  val dummyDesHeaderCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      Seq("^not-test-BaseUrl?$".r),
      Seq.empty[String],
      Some("mtd-api")
    )

  val dummyIfsHeaderCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      Seq("^not-test-BaseUrl?$".r),
      Seq.empty[String],
      Some("mtd-api")
    )

  val dummyHeaderCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      Seq("^not-test-BaseUrl?$".r),
      Seq.empty[String],
      Some("mtd-api")
    )

  val allowedDesHeaders: Seq[String] = Seq(
    "Accept",
    "Gov-Test-Scenario",
    "Content-Type",
    "Location",
    "X-Request-Timestamp",
    "X-Session-Id"
  )

  val allowedIfsHeaders: Seq[String] = Seq(
    "Accept",
    "Gov-Test-Scenario",
    "Content-Type",
    "Location",
    "X-Request-Timestamp",
    "X-Session-Id"
  )

  val requiredDesHeaders: Seq[(String, String)] = Seq(
    "Environment"   -> "des-environment",
    "Authorization" -> s"Bearer des-token",
    "CorrelationId" -> s"$correlationId"
  )

  val requiredIfsHeaders: Seq[(String, String)] = Seq(
    "Environment"   -> "ifs-environment",
    "Authorization" -> s"Bearer ifs-token",
    "CorrelationId" -> s"$correlationId"
  )

  val requiredTysIfsHeaders: Seq[(String, String)] = Seq(
    "Environment"   -> "TYS-IFS-environment",
    "Authorization" -> s"Bearer TYS-IFS-token",
    "CorrelationId" -> s"$correlationId"
  )

  val requiredRelease6Headers: Seq[(String, String)] = Seq(
    "Environment"   -> "release6-environment",
    "Authorization" -> s"Bearer release6-token",
    "CorrelationId" -> s"$correlationId"
  )

  val requiredApi1661Headers: Seq[(String, String)] = Seq(
    "Environment"   -> "api1661-environment",
    "Authorization" -> s"Bearer api1661-token",
    "CorrelationId" -> s"$correlationId"
  )

  protected trait ConnectorTest extends MockHttpClient with MockAppConfig {
    protected val baseUrl: String = "http://test-BaseUrl"

    implicit protected val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders)

    protected val requiredHeaders: Seq[(String, String)]

    protected def willGet[T](url: String): CallHandler[Future[T]] = {
      MockedHttpClient
        .get(
          url = url,
          config = dummyHeaderCarrierConfig,
          requiredHeaders = requiredHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
    }

    protected def willGet[T](url: String, queryParams: Seq[(String, String)]): CallHandler[Future[T]] = {
      MockedHttpClient
        .get(
          url = url,
          parameters = queryParams,
          config = dummyHeaderCarrierConfig,
          requiredHeaders = requiredHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
    }

    protected def willPost[BODY, T](url: String, body: BODY): CallHandler[Future[T]] = {
      MockedHttpClient
        .post(
          url = url,
          config = dummyHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredHeaders ++ Seq("Content-Type" -> "application/json"),
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
    }

    protected def willPut[BODY, T](url: String, body: BODY): CallHandler[Future[T]] = {
      MockedHttpClient
        .put(
          url = url,
          config = dummyHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredHeaders ++ Seq("Content-Type" -> "application/json"),
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
    }

    protected def willDelete[T](url: String): CallHandler[Future[T]] = {
      MockedHttpClient
        .delete(
          url = url,
          config = dummyHeaderCarrierConfig,
          requiredHeaders = requiredHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
    }

  }

  protected trait DesTest extends ConnectorTest {

    protected lazy val requiredHeaders: Seq[(String, String)] = requiredDesHeaders

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)

    MockedAppConfig.featureSwitches returns Configuration("tys-api.enabled" -> false)

  }

  protected trait IfsTest extends ConnectorTest {

    protected lazy val requiredHeaders: Seq[(String, String)] = requiredIfsHeaders

    MockedAppConfig.ifsBaseUrl.returns(baseUrl)
    MockedAppConfig.ifsToken.returns("ifs-token")
    MockedAppConfig.ifsEnvironment.returns("ifs-environment")
    MockedAppConfig.ifsEnvironmentHeaders.returns(Some(allowedIfsHeaders))

    MockedAppConfig.featureSwitches returns Configuration("tys-api.enabled" -> false)

  }

  protected trait TysIfsTest extends ConnectorTest {

    protected def taxYear: TaxYear

    protected lazy val requiredHeaders: Seq[(String, String)] = requiredTysIfsHeaders

    MockedAppConfig.tysIfsBaseUrl.returns(baseUrl)
    MockedAppConfig.tysIfsToken.returns("TYS-IFS-token")
    MockedAppConfig.tysIfsEnvironment.returns("TYS-IFS-environment")
    MockedAppConfig.tysIfsEnvironmentHeaders.returns(Some(allowedIfsHeaders))

    MockedAppConfig.featureSwitches.returns(Configuration("tys-api.enabled" -> true))
  }

}
