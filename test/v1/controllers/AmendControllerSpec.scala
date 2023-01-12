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

package v1.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.AmendRequestFixtures._
import v1.mocks.MockIdGenerator
import v1.mocks.requestParsers.MockAmendRequestParser
import v1.mocks.services.{MockAmendService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.domain.{Nino, TaxYear}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amend.{AmendRawData, AmendRequestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendRequestParser
    with MockAmendService
    with MockAuditService
    with MockIdGenerator {

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockAmendRequestParser,
      service = mockAmendService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val nino          = "AA123456A"
  private val submissionId  = "S4636A77V5KB8625U"
  private val correlationId = "X-123"
  private val taxYear       = TaxYear.fromIso("2019-07-05")

  private val rawData     = AmendRawData(nino, submissionId, requestJson)
  private val requestData = AmendRequestData(Nino(nino), submissionId, taxYear, amendRequestObj)

  private val rawMissingOptionalAmendRequest = AmendRawData(nino, submissionId, missingOptionalRequestJson)
  private val missingOptionalAmendRequest    = AmendRequestData(Nino(nino), submissionId, taxYear, amendMissingOptionalRequestObj)

  def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "AmendCisDeductionsForSubcontractor",
      transactionName = "amend-cis-deductions-for-subcontractor",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        nino,
        Some(submissionId),
        `X-CorrelationId` = correlationId,
        requestBody,
        auditResponse
      )
    )

  "requestData" should {

    "return a successful response with status 204 (NO CONTENT)" when {

      "a valid request is supplied for a cis PUT request" in new Test {

        MockAmendRequestDataParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendService
          .submitAmendRequest(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, None))))

        val result: Future[Result] = controller.amendRequest(nino, submissionId)(fakePostRequest(Json.toJson(requestJson)))

        status(result) shouldBe NO_CONTENT
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(NO_CONTENT, None, None)
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(requestJson))).once()
      }

      "a valid request is supplied when an optional field is missing" in new Test {

        MockAmendRequestDataParser
          .parse(rawMissingOptionalAmendRequest)
          .returns(Right(missingOptionalAmendRequest))

        MockAmendService
          .submitAmendRequest(missingOptionalAmendRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, None))))

        val result: Future[Result] = controller.amendRequest(nino, submissionId)(fakePostRequest(Json.toJson(missingOptionalRequestJson)))

        status(result) shouldBe NO_CONTENT
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(NO_CONTENT, None, None)
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(missingOptionalRequestJson))).once()
      }
    }
  }

  "return errors as per the spec" when {

    def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
      s"a ${error.code} error is returned from the parser" in new Test {

        MockAmendRequestDataParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, error, None)))

        val result: Future[Result] = controller.amendRequest(nino, submissionId)(fakePostRequest(requestJson))

        status(result) shouldBe expectedStatus
        contentAsJson(result) shouldBe Json.toJson(error)
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(requestJson))).once()
      }
    }

    val input = Seq(
      (BadRequestError, BAD_REQUEST),
      (NinoFormatError, BAD_REQUEST),
      (DeductionFromDateFormatError, BAD_REQUEST),
      (DeductionToDateFormatError, BAD_REQUEST),
      (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
      (RuleDeductionsDateRangeInvalidError, BAD_REQUEST),
      (RuleDeductionAmountError, BAD_REQUEST),
      (RuleCostOfMaterialsError, BAD_REQUEST),
      (RuleGrossAmountError, BAD_REQUEST),
      (SubmissionIdFormatError, BAD_REQUEST),
      (StandardDownstreamError, INTERNAL_SERVER_ERROR)
    )

    input.foreach(args => (errorsFromParserTester _).tupled(args))

    "multiple parser errors occur" in new Test {

      val error: ErrorWrapper = ErrorWrapper(correlationId, BadRequestError, Some(Seq(BadRequestError, NinoFormatError)))

      MockAmendRequestDataParser
        .parse(rawData)
        .returns(Left(error))

      val result: Future[Result] = controller.amendRequest(nino, submissionId)(fakePostRequest(Json.toJson(requestJson)))

      status(result) shouldBe BAD_REQUEST
      contentAsJson(result) shouldBe Json.toJson(error)
      header("X-CorrelationId", result) shouldBe Some(correlationId)

      val auditResponse: AuditResponse =
        AuditResponse(BAD_REQUEST, Some(Seq(AuditError(BadRequestError.code), AuditError(NinoFormatError.code))), None)
      MockedAuditService.verifyAuditEvent(event(auditResponse, Some(requestJson))).once()
    }

    "multiple errors occur for format errors" in new Test {

      val error: ErrorWrapper = ErrorWrapper(
        correlationId,
        BadRequestError,
        Some(
          Seq(
            BadRequestError,
            DeductionToDateFormatError,
            DeductionFromDateFormatError,
            ToDateFormatError,
            FromDateFormatError,
            TaxYearFormatError
          ))
      )

      MockAmendRequestDataParser
        .parse(rawData)
        .returns(Left(error))

      val result: Future[Result] = controller.amendRequest(nino, submissionId)(fakePostRequest(Json.toJson(requestJson)))

      status(result) shouldBe BAD_REQUEST
      contentAsJson(result) shouldBe Json.toJson(error)
      header("X-CorrelationId", result) shouldBe Some(correlationId)

      val auditResponse: AuditResponse = AuditResponse(
        BAD_REQUEST,
        Some(
          Seq(
            AuditError(BadRequestError.code),
            AuditError(DeductionToDateFormatError.code),
            AuditError(DeductionFromDateFormatError.code),
            AuditError(ToDateFormatError.code),
            AuditError(FromDateFormatError.code),
            AuditError(TaxYearFormatError.code)
          )),
        None
      )

      MockedAuditService.verifyAuditEvent(event(auditResponse, Some(requestJson))).once()
    }
  }

  "return downstream errors as per the spec" when {

    def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
      s"a ${mtdError.code} error is returned from the service" in new Test {

        MockAmendRequestDataParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendService
          .submitAmendRequest(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

        val result: Future[Result] = controller.amendRequest(nino, submissionId)(fakePostRequest(Json.toJson(requestJson)))

        status(result) shouldBe expectedStatus
        contentAsJson(result) shouldBe Json.toJson(mtdError)
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(requestJson))).once()
      }
    }

    val input = Seq(
      (BadRequestError, BAD_REQUEST),
      (NotFoundError, NOT_FOUND),
      (NinoFormatError, BAD_REQUEST),
      (RuleDeductionsDateRangeInvalidError, BAD_REQUEST),
      (RuleTaxYearNotSupportedError, BAD_REQUEST),
      (RuleUnalignedDeductionsPeriodError, BAD_REQUEST),
      (RuleDuplicatePeriodError, BAD_REQUEST),
      (SubmissionIdFormatError, BAD_REQUEST),
      (StandardDownstreamError, INTERNAL_SERVER_ERROR)
    )

    input.foreach(args => (serviceErrors _).tupled(args))
  }

}
