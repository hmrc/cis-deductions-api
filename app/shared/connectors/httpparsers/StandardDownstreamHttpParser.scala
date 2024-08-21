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

package shared.connectors.httpparsers

import play.api.http.Status._
import play.api.libs.json.Reads
import shared.connectors.DownstreamOutcome
import shared.models.errors.{InternalError, OutboundError}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object StandardDownstreamHttpParser extends HttpParser {

  case class SuccessCode(status: Int) extends AnyVal

  // Return Right[IfsResponse[Unit]] as success response has no body - no need to assign it a value
  implicit def readsEmpty(implicit successCode: SuccessCode = SuccessCode(NO_CONTENT)): HttpReads[DownstreamOutcome[Unit]] =
    (_: String, url: String, response: HttpResponse) =>
      doRead(url, response) { correlationId =>
        Right(ResponseWrapper(correlationId, ()))
      }

  implicit def reads[A: Reads](implicit successCode: SuccessCode = SuccessCode(OK)): HttpReads[DownstreamOutcome[A]] =
    (_: String, url: String, response: HttpResponse) =>
      doRead(url, response) { correlationId =>
        response.validateJson[A] match {
          case Some(ref) => Right(ResponseWrapper(correlationId, ref))
          case None      => Left(ResponseWrapper(correlationId, OutboundError(InternalError)))
        }
      }

  private def doRead[A](url: String, response: HttpResponse)(successOutcomeFactory: String => DownstreamOutcome[A])(implicit
      successCode: SuccessCode): DownstreamOutcome[A] = {

    val correlationId = retrieveCorrelationId(response)

    if (response.status != successCode.status) {
      logger.warn(
        "[StandardDownstreamHttpParser][read] - " +
          s"Error response received from downstream with status: ${response.status} and body\n" +
          s"${response.body} and correlationId: $correlationId when calling $url")
    }

    response.status match {
      case successCode.status =>
        logger.info(
          "[StandardDownstreamHttpParser][read] - " +
            s"Success response received from downstream with correlationId: $correlationId when calling $url")
        successOutcomeFactory(correlationId)

      case BAD_REQUEST | NOT_FOUND | FORBIDDEN | CONFLICT | UNPROCESSABLE_ENTITY | GONE =>
        Left(ResponseWrapper(correlationId, parseErrors(response)))

      case _ =>
        Left(ResponseWrapper(correlationId, OutboundError(InternalError)))
    }
  }

}
