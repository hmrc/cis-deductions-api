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

package v1.mocks

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

trait MockHttpClient extends MockFactory {
  val mockHttpClient: HttpClient = mock[HttpClient]

  object MockedHttpClient {

    def get[T](url: String,
               config: HeaderCarrier.Config,
               requiredHeaders: Seq[(String, String)] = Seq.empty,
               excludedHeaders: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {
      (mockHttpClient
        .GET(_: String, _: Seq[(String, String)], _: Seq[(String, String)])(_: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(where {
          (actualUrl: String, _: Seq[(String, String)], _: Seq[(String, String)], _: HttpReads[T], hc: HeaderCarrier, _: ExecutionContext) =>
          {
            val headersForUrl = hc.headersForUrl(config)(actualUrl)
            url == actualUrl &&
              requiredHeaders.forall(h => headersForUrl.contains(h)) &&
              excludedHeaders.forall(h => !headersForUrl.contains(h))
          }
        })
    }

    def parameterGet[T](url: String,
                        parameters: Seq[(String, String)],
                        config: HeaderCarrier.Config,
                        requiredHeaders: Seq[(String, String)] = Seq.empty,
                        excludedHeaders: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {
      (mockHttpClient
        .GET(_: String, _: Seq[(String, String)], _: Seq[(String, String)])(_: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(where {
          (actualUrl: String, params: Seq[(String, String)], _: Seq[(String, String)], _: HttpReads[T], hc: HeaderCarrier, _: ExecutionContext) =>
          {
            val headersForUrl = hc.headersForUrl(config)(actualUrl)
            url == actualUrl &&
              requiredHeaders.forall(h => headersForUrl.contains(h)) &&
              excludedHeaders.forall(h => !headersForUrl.contains(h)) &&
              params == parameters
          }
        })
    }

    def delete[T](url: String,
                  config: HeaderCarrier.Config,
                  requiredHeaders: Seq[(String, String)] = Seq.empty,
                  excludedHeaders: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {
      (mockHttpClient
        .DELETE(_: String, _: Seq[(String, String)])(_: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(where {
          (actualUrl: String, _: Seq[(String, String)], _: HttpReads[T], hc: HeaderCarrier, _: ExecutionContext) => {
            val headersForUrl = hc.headersForUrl(config)(actualUrl)
            url == actualUrl &&
              requiredHeaders.forall(h => headersForUrl.contains(h)) &&
              excludedHeaders.forall(h => !headersForUrl.contains(h))
          }
        })
    }

    def post[I, T](url: String,
                   config: HeaderCarrier.Config,
                   body: I,
                   requiredHeaders: Seq[(String, String)] = Seq.empty,
                   excludedHeaders: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {
      (mockHttpClient
        .POST[I, T](_: String, _: I, _: Seq[(String, String)])(_: Writes[I], _: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(where { (actualUrl: String, actualBody: I, _, _, _, hc: HeaderCarrier, _) =>
        {
          val headersForUrl = hc.headersForUrl(config)(actualUrl)
          url == actualUrl && body == actualBody &&
            requiredHeaders.forall(h => headersForUrl.contains(h)) &&
            excludedHeaders.forall(h => !headersForUrl.contains(h))
        }
        })
    }

    def put[I, T](url: String,
                  config: HeaderCarrier.Config,
                  body: I,
                  requiredHeaders: Seq[(String, String)] = Seq.empty,
                  excludedHeaders: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {
      (mockHttpClient
        .PUT[I, T](_: String, _: I, _: Seq[(String, String)])(_: Writes[I], _: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(where { (actualUrl: String, actualBody: I, _, _, _, hc: HeaderCarrier, _) =>
        {
          val headersForUrl = hc.headersForUrl(config)(actualUrl)
          url == actualUrl && body == actualBody &&
            requiredHeaders.forall(h => headersForUrl.contains(h)) &&
            excludedHeaders.forall(h => !headersForUrl.contains(h))
        }
        })
    }

  }

}
