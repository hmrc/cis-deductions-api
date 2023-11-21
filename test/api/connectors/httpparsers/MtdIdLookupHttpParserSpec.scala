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

package api.connectors.httpparsers

import api.connectors.MtdIdLookupOutcome
import api.connectors.httpparsers.MtdIdLookupHttpParser.mtdIdLookupHttpReads
import play.api.libs.json.Writes.StringWrites
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{FORBIDDEN, INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import shared.UnitSpec
import shared.models.errors.{InternalError, InvalidBearerTokenError, NinoFormatError}
import uk.gov.hmrc.http.HttpResponse

class MtdIdLookupHttpParserSpec extends UnitSpec {

  val method = "GET"
  val url    = "test-url"
  val mtdId  = "test-mtd-id"

  val mtdIdJson: JsObject   = Json.obj("mtdbsa" -> mtdId)
  val invalidJson: JsObject = Json.obj("hello" -> "world")

  val emptyHeaders: Map[String, Seq[String]] = Map[String, Seq[String]]()

  "read" should {
    "return an MtdId" when {
      "the HttpResponse contains a 200 status and a correct response body" in {
        val response                   = HttpResponse(OK, mtdIdJson, emptyHeaders)
        val result: MtdIdLookupOutcome = mtdIdLookupHttpReads.read(method, url, response)

        result shouldBe Right(mtdId)
      }
    }

    "returns an downstream error" when {
      "backend doesn't have a valid data" in {
        val response                   = HttpResponse(OK, invalidJson, emptyHeaders)
        val result: MtdIdLookupOutcome = mtdIdLookupHttpReads.read(method, url, response)

        result shouldBe Left(InternalError)
      }

      "backend doesn't return any data" in {
        val response                   = HttpResponse(OK, "", emptyHeaders)
        val result: MtdIdLookupOutcome = mtdIdLookupHttpReads.read(method, url, response)

        result shouldBe Left(InternalError)
      }

      "the json cannot be read" in {
        val response                   = HttpResponse(OK, "{", emptyHeaders)
        val result: MtdIdLookupOutcome = mtdIdLookupHttpReads.read(method, url, response)

        result shouldBe Left(InternalError)
      }
    }

    "return an InvalidNino error" when {
      "the HttpResponse contains a 403 status" in {
        val response                   = HttpResponse(FORBIDDEN, "", emptyHeaders)
        val result: MtdIdLookupOutcome = mtdIdLookupHttpReads.read(method, url, response)

        result shouldBe Left(NinoFormatError)
      }
    }

    "return an Unauthorised error" when {
      "the HttpResponse contains a 403 status" in {
        val response                   = HttpResponse(UNAUTHORIZED, "", emptyHeaders)
        val result: MtdIdLookupOutcome = mtdIdLookupHttpReads.read(method, url, response)

        result shouldBe Left(InvalidBearerTokenError)
      }
    }

    "return a DownstreamError" when {
      "the HttpResponse contains any other status" in {
        val response                   = HttpResponse(INTERNAL_SERVER_ERROR, "", emptyHeaders)
        val result: MtdIdLookupOutcome = mtdIdLookupHttpReads.read(method, url, response)

        result shouldBe Left(InternalError)
      }
    }
  }

}
