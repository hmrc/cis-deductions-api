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

package shared.connectors

import com.google.common.base.Charsets
import shared.config.{SharedAppConfig, BasicAuthDownstreamConfig, ConfigFeatureSwitches, DownstreamConfig}

import java.util.Base64
import scala.concurrent.{ExecutionContext, Future}

/** Represents a strategy for determining necessary HTTP parameters to make a request to a downstream service.
  */
trait DownstreamStrategy {

  /** Gets the base HTTP/HTTPS URL for the host
    */
  def baseUrl: String

  /** Gets the contract headers that the services require. This includes any Authorization headers, and returns a future to allow for any required
    * non-blocking retrieval of tokens e.g. from an OAuth service.
    */
  def contractHeaders(correlationId: String)(implicit ec: ExecutionContext): Future[Seq[(String, String)]]

  /** Gets the headers in the MTD request that are to be passed through to the downstream service. This includes request tracking headers and
    * gov-test-scenario headers for when the downstream host is a stub.
    */
  def environmentHeaders: Seq[String]
}

object DownstreamStrategy {

  /** Creates a strategy instance that uses a fixed token for each host/endpoint
    * @param downstreamConfig
    *   configuration for the downstream host & endpoint
    */
  def standardStrategy(downstreamConfig: DownstreamConfig): DownstreamStrategy = new DownstreamStrategy {
    override def baseUrl: String = downstreamConfig.baseUrl

    override def contractHeaders(correlationId: String)(implicit ec: ExecutionContext): Future[Seq[(String, String)]] = {
      Future.successful(
        List(
          "Authorization" -> s"Bearer ${downstreamConfig.token}",
          "Environment"   -> downstreamConfig.env,
          "CorrelationId" -> correlationId
        ))
    }

    override def environmentHeaders: Seq[String] = downstreamConfig.environmentHeaders.getOrElse(Nil)
  }

  /** Creates a strategy instance that uses the OAuth client id and secret but as a base64-encoded Basic auth token.
    * @param downstreamConfig
    *   configuration for the downstream host & endpoint
    */
  def basicAuthStrategy(downstreamConfig: BasicAuthDownstreamConfig): DownstreamStrategy = new DownstreamStrategy {
    override def baseUrl: String = downstreamConfig.baseUrl

    override def contractHeaders(correlationId: String)(implicit ec: ExecutionContext): Future[Seq[(String, String)]] = {
      val encodedToken = Base64.getEncoder.encodeToString(s"${downstreamConfig.clientId}:${downstreamConfig.clientSecret}".getBytes(Charsets.UTF_8))

      Future.successful(
        List(
          "Authorization" -> s"Basic $encodedToken",
          "Environment"   -> downstreamConfig.env,
          "CorrelationId" -> correlationId
        ))
    }

    override def environmentHeaders: Seq[String] = downstreamConfig.environmentHeaders.getOrElse(Nil)
  }

  /** Creates a strategy based on a choice of two strategies and the value of a feature switch.
    * @param onStrategy
    *   the strategy to use when the switch is enabled
    * @param offStrategy
    *   the strategy to use when the switch is disabled
    */
  def switchedStrategy(onStrategy: => DownstreamStrategy, offStrategy: => DownstreamStrategy, switchName: String)(implicit
      appConfig: SharedAppConfig): DownstreamStrategy =
    if (ConfigFeatureSwitches().isEnabled(switchName)) onStrategy else offStrategy

}
