/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers._
import v1.mocks.services.{MockEnrolmentsAuthService, _}
import v1.models.audit._
import v1.models.request._
import v1.fixtures.CreateRequestFixtures._
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.responseData.CreateResponseModel

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateRequestControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateRequestParser
    with MockCreateService
    with MockAuditService {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new CreateRequestController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRequestDataParser,
      service = mockService,
      auditService = mockAuditService,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()

  }

  private val nino = "AA123456A"
  private val correlationId = "X-123"

  private val responseId = "S4636A77V5KB8625U"

  private val rawCreateRequest = CreateRawData(nino, requestJson)
  private val createRequest = CreateRequestData(Nino(nino), requestObj)

  private val rawMissingOptionalCreateRequest = CreateRawData(nino, missingOptionalRequestJson)
  private val missingOptionalCreateRequest = CreateRequestData(Nino(nino), missingOptionalRequestObj)

  val response = CreateResponseModel(responseId)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "createCisDeductionsAuditType",
        transactionName = "create-cis-deductions-transaction-type",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          nino,
          `X-CorrelationId` = correlationId,
          auditResponse
        )
      )

  "createRequest" should {
    "return a successful response with status 200 (OK)" when {
      "a valid request is supplied for a cis post request" in new Test {

        MockCreateRequestDataParser
          .parse(rawCreateRequest)
          .returns(Right(createRequest))

        MockCreateService
          .submitCreateRequest(createRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        val result: Future[Result] = controller.createRequest(nino)(fakePostRequest(Json.toJson(requestJson)))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe responseJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(responseJson))
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(responseJson))).once
      }

      "a valid request is supplied when an optional field is missing" in new Test {

        MockCreateRequestDataParser
          .parse(rawMissingOptionalCreateRequest)
          .returns(Right(missingOptionalCreateRequest))

        MockCreateService
          .submitCreateRequest(missingOptionalCreateRequest)
          .returns(Future.successful((Right(ResponseWrapper(correlationId, response)))))

        val result: Future[Result] = controller.createRequest(nino)(fakePostRequest(Json.toJson(missingOptionalRequestJson)))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe responseJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(responseJson))
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(responseJson))).once
      }
    }

    "return errors as per the spec" when {
      def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
        s"a ${error.code} error is returned from the parser" in new Test {

          MockCreateRequestDataParser
            .parse(rawCreateRequest)
            .returns(Left(ErrorWrapper(Some(correlationId), Seq(error))))

          val result: Future[Result] = controller.createRequest(nino)(fakePostRequest(requestJson))

          status(result) shouldBe expectedStatus
          contentAsJson(result) shouldBe Json.toJson(error)
          header("X-CorrelationId", result) shouldBe Some(correlationId)

          val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
          MockedAuditService.verifyAuditEvent(event(auditResponse, Some(responseJson))).once
        }
      }

      val input = Seq(
        (BadRequestError, BAD_REQUEST),
        (NinoFormatError, BAD_REQUEST),
        (DeductionFromDateFormatError, BAD_REQUEST),
        (DeductionToDateFormatError, BAD_REQUEST),
        (FromDateFormatError, BAD_REQUEST),
        (ToDateFormatError, BAD_REQUEST),
        (RuleToDateBeforeFromDateError, BAD_REQUEST),
        (RuleDeductionsDateRangeInvalidError, BAD_REQUEST),
        (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
        (RuleDeductionAmountError, BAD_REQUEST),
        (RuleCostOfMaterialsError, BAD_REQUEST),
        (RuleGrossAmountError, BAD_REQUEST),
        (DownstreamError, INTERNAL_SERVER_ERROR),
      )

      input.foreach(args => (errorsFromParserTester _).tupled(args))

      "multiple parser errors occur" in new Test {
        val error = ErrorWrapper(Some(correlationId), Seq(BadRequestError, NinoFormatError))

        MockCreateRequestDataParser
          .parse(rawCreateRequest)
          .returns(Left(error))

        val result: Future[Result] = controller.createRequest(nino)(fakePostRequest(Json.toJson(requestJson)))

        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe Json.toJson(error)
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(BAD_REQUEST, Some(Seq(AuditError(BadRequestError.code), AuditError(NinoFormatError.code))), None)
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(responseJson))).once
      }

      "multiple errors occur for format errors" in new Test {
        val error = ErrorWrapper(
          Some(correlationId),
          Seq(
            BadRequestError,
            DeductionToDateFormatError,
            DeductionFromDateFormatError,
            ToDateFormatError,
            FromDateFormatError,
            TaxYearFormatError
          )
        )

        MockCreateRequestDataParser
          .parse(rawCreateRequest)
          .returns(Left(error))

        val result: Future[Result] = controller.createRequest(nino)(fakePostRequest(Json.toJson(requestJson)))

        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe Json.toJson(error)
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(BAD_REQUEST, Some(
          Seq(
            AuditError(BadRequestError.code),
            AuditError(DeductionToDateFormatError.code),
            AuditError(DeductionFromDateFormatError.code),
            AuditError(ToDateFormatError.code),
            AuditError(FromDateFormatError.code),
            AuditError(TaxYearFormatError.code))),
          None
        )

        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(responseJson))).once
      }
    }

    "return downstream errors as per the spec" when {
      def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
        s"a ${mtdError.code} error is returned from the service" in new Test {

          MockCreateRequestDataParser
            .parse(rawCreateRequest)
            .returns(Right(createRequest))

          MockCreateService
            .submitCreateRequest(createRequest)
            .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), Seq(mtdError)))))

          val result: Future[Result] = controller.createRequest(nino)(fakePostRequest(Json.toJson(requestJson)))

          status(result) shouldBe expectedStatus
          contentAsJson(result) shouldBe Json.toJson(mtdError)
          header("X-CorrelationId", result) shouldBe Some(correlationId)

          val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
          MockedAuditService.verifyAuditEvent(event(auditResponse, Some(responseJson))).once
        }
      }

      val input = Seq(
        (NinoFormatError, BAD_REQUEST),
        (NotFoundError, NOT_FOUND),
        (DownstreamError, INTERNAL_SERVER_ERROR),
        (RuleTaxYearNotSupportedError, BAD_REQUEST),
        (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
        (RuleTaxYearRangeExceededError, BAD_REQUEST),
        (DeductionFromDateFormatError, BAD_REQUEST),
        (DeductionToDateFormatError, BAD_REQUEST),
        (FromDateFormatError, BAD_REQUEST),
        (ToDateFormatError, BAD_REQUEST),
        (RuleToDateBeforeFromDateError, BAD_REQUEST),
        (RuleDeductionsDateRangeInvalidError, BAD_REQUEST),
      )
      input.foreach(args => (serviceErrors _).tupled(args))
    }
  }
}
