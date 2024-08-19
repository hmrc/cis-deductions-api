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

package shared.utils

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.http.Status
import play.api.http.Status.UNSUPPORTED_MEDIA_TYPE
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, RequestHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import shared.models.errors._
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.http.{HeaderCarrier, JsValidationException, NotFoundException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.model.{DataEvent, TruncationLog}
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

class ErrorHandlerSpec extends UnitSpec with GuiceOneAppPerSuite {

  "onClientError" should {
    "return 404 with error body" when {
      s"URI not found" in new Test {

        val result: Future[Result] = handler.onClientError(requestHeader, Status.NOT_FOUND, "test")
        status(result) shouldBe Status.NOT_FOUND

        contentAsJson(result) shouldBe NotFoundError.asJson
      }
    }

    "return 400 with error body" when {
      "JsValidationException thrown and header is supplied" in new Test {
        val result: Future[Result] = handler.onClientError(requestHeader, BAD_REQUEST, "test")
        status(result) shouldBe BAD_REQUEST

        contentAsJson(result) shouldBe BadRequestError.asJson
      }
    }

    "return 401 with error body" when {
      "unauthorised and header is supplied" in new Test {
        val result: Future[Result] = handler.onClientError(requestHeader, UNAUTHORIZED, "test")
        status(result) shouldBe UNAUTHORIZED

        contentAsJson(result) shouldBe ClientOrAgentNotAuthorisedError.asJson
      }
    }

    "return 415 with error body" when {
      "unsupported body and header is supplied" in new Test {
        val result: Future[Result] = handler.onClientError(requestHeader, UNSUPPORTED_MEDIA_TYPE, "test")
        status(result) shouldBe UNSUPPORTED_MEDIA_TYPE

        contentAsJson(result) shouldBe InvalidBodyTypeError.asJson
      }
    }

    "return 405 with error body" when {
      "invalid method type" in new Test {
        val result: Future[Result] = handler.onClientError(requestHeader, METHOD_NOT_ALLOWED, "test")
        status(result) shouldBe METHOD_NOT_ALLOWED

        contentAsJson(result) shouldBe InvalidHttpMethodError.asJson
      }
    }

    "default to an INVALID_REQUEST" when {
      "no matching error was found" in new Test {
        val result: Future[Result] = handler.onClientError(requestHeader, statusCode = 499, "unmatched error")

        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe Json.parse(
          """
          |{
          |  "code": "INVALID_REQUEST",
          |  "message": "unmatched error"
          |}
          |""".stripMargin
        )
      }
    }
  }

  "onServerError" should {

    "return 404 with error body" when {
      "NotFoundException thrown" in new Test {
        val result: Future[Result] = handler.onServerError(requestHeader, new NotFoundException("test") with NoStackTrace)
        status(result) shouldBe NOT_FOUND

        contentAsJson(result) shouldBe NotFoundError.asJson
      }
    }

    "return 401 with error body" when {
      "AuthorisationException thrown" in new Test {
        val result: Future[Result] = handler.onServerError(requestHeader, new InsufficientEnrolments("test") with NoStackTrace)
        // TODO This really should be FORBIDDEN (403), but would need to be changed across all the APIs at once (if at all).
        status(result) shouldBe UNAUTHORIZED

        contentAsJson(result) shouldBe ClientOrAgentNotAuthorisedError.asJson
      }
    }

    "return 400 with error body" when {
      "JsValidationException thrown" in new Test {
        val result: Future[Result] =
          handler.onServerError(requestHeader, new JsValidationException("test", "test", classOf[String], "errs") with NoStackTrace)
        status(result) shouldBe BAD_REQUEST

        contentAsJson(result) shouldBe BadRequestError.asJson
      }
    }

    "return 500 with error body" when {
      "other exception thrown" in new Test {
        val result: Future[Result] = handler.onServerError(requestHeader, new Exception with NoStackTrace)
        status(result) shouldBe INTERNAL_SERVER_ERROR

        contentAsJson(result) shouldBe InternalError.asJson
      }
    }
  }

  def anyVersionHeader: (String, String) = ACCEPT -> s"application/vnd.hmrc.1.0+json"

  class Test {
    val method = "some-method"

    val requestHeader: FakeRequest[AnyContent] = FakeRequest().withHeaders(anyVersionHeader)

    val auditConnector: AuditConnector = mock[AuditConnector]
    val httpAuditEvent: HttpAuditEvent = mock[HttpAuditEvent]

    val eventTags: Map[String, String] = Map("transactionName" -> "event.transactionName")

    val dataEvent: DataEvent = DataEvent(
      auditSource = "auditSource",
      auditType = "event.auditType",
      eventId = "",
      tags = eventTags,
      detail = Map("test" -> "test"),
      generatedAt = Instant.now()
    )

    (httpAuditEvent
      .dataEvent(_: String, _: String, _: RequestHeader, _: Map[String, String], _: TruncationLog)(_: HeaderCarrier))
      .expects(*, *, *, *, *, *)
      .returns(dataEvent)

    (auditConnector
      .sendEvent(_: DataEvent)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returns(Future.successful(Success))

    val configuration: Configuration = Configuration(
      "appName"                                         -> "myApp",
      "bootstrap.errorHandler.warnOnly.statusCodes"     -> List(OK),
      "bootstrap.errorHandler.suppress4xxErrorMessages" -> false,
      "bootstrap.errorHandler.suppress5xxErrorMessages" -> false
    )

    val handler = new ErrorHandler(configuration, auditConnector, httpAuditEvent)
  }

}
