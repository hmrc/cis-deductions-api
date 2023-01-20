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

package api.controllers

import play.api.http.HttpEntity
import play.api.mvc.{ResponseHeader, Result}
import support.UnitSpec
import utils.Logging

class BaseControllerSpec extends UnitSpec {

  class TestController extends BaseController with Logging

  val twoHundred = 200

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "BaseController", endpointName = "beans")

  val result        = Result(ResponseHeader(twoHundred), HttpEntity.NoEntity)
  val correlationId = "X-123"

  val baseHeaders: Map[String, String] =
    Map("X-CorrelationId" -> correlationId, "X-Content-Type-Options" -> "nosniff", "Content-Type" -> "application/json")

  "BaseController" when {
    "withApiHeaders is called" should {
      "return the response with API headers" when {
        "no extra headers are passed in" in new TestController {
          val response = new Response(result)

          response.withApiHeaders("X-123") shouldBe result.copy(header = result.header.copy(headers = baseHeaders.toMap))
        }
        "extra headers are passed in" in new TestController {
          val response = new Response(result)

          val extraHeaders = ("key" -> "value")

          response.withApiHeaders("X-123", extraHeaders) shouldBe result.copy(header = result.header.copy(headers = baseHeaders + extraHeaders))
        }
      }
    }
  }

}
