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

import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.RetrieveJson._
import v1.mocks.requestParsers.MockDeleteRequestDataParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, _}
import v1.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteControllerSpec extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDeleteRequestDataParser
    with MockDeleteService
    with MockAppConfig
    with MockAuditService {

    trait Test {
        val hc = HeaderCarrier()

        val controller = new DeleteController(
            authService = mockEnrolmentsAuthService,
            lookupService = mockMtdIdLookupService,
            requestParser = mockRequestParser,
            service = mockDeleteService,
            auditService = mockAuditService,
            cc = cc
        )

        MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
        MockedEnrolmentsAuthService.authoriseUser()

    }

    private val nino = "AA123456A"
    private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
    private val correlationId = "X-123"
    private val deleteRawData = DeleteRawData(nino, submissionId)
    private val deleteRequestData = DeleteRequestData(Nino(nino), submissionId)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
        AuditEvent(
            auditType = "deleteCisDeductionsAuditType",
            transactionName = "delete-cis-deductions-transaction-type",
            detail = GenericAuditDetail(
                userType = "Individual",
                agentReferenceNumber = None,
                nino,
                `X-CorrelationId` = correlationId,
                auditResponse
            )
        )

    "DeleteCis" should {
        "return a successful response with status 204 (No Content)" when {
            "a valid request is supplied for a cis get request" in new Test {

                MockDeleteRequestDataParser
                  .parse(deleteRawData)
                  .returns(Right(deleteRequestData))

                MockDeleteService
                  .deleteRequest(deleteRequestData)
                  .returns(Future.successful(Right(ResponseWrapper(correlationId, None))))

                val result: Future[Result] = controller.deleteRequest(nino, submissionId)(fakeRequest)

                status(result) shouldBe NO_CONTENT
                header("X-CorrelationId", result) shouldBe Some(correlationId)

                val auditResponse: AuditResponse = AuditResponse(NO_CONTENT, None, None)
                MockedAuditService.verifyAuditEvent(event(auditResponse, None)).once()

            }
        }

        "return the error as per spec" when {
            def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
                s"a ${error.code} error is returned from the parser" in new Test {

                    MockDeleteRequestDataParser
                      .parse(deleteRawData)
                      .returns(Left(ErrorWrapper(Some(correlationId), Seq(error))))

                    val result: Future[Result] = controller.deleteRequest(nino, submissionId)(fakeRequest)


                    status(result) shouldBe expectedStatus
                    contentAsJson(result) shouldBe Json.toJson(error)
                    header("X-CorrelationId", result) shouldBe Some(correlationId)

                    val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
                    MockedAuditService.verifyAuditEvent(event(auditResponse, None)).once()

                }
            }

            val input = Seq(
                (BadRequestError, BAD_REQUEST),
                (NinoFormatError, BAD_REQUEST),
                (DownstreamError, INTERNAL_SERVER_ERROR),
            )
            input.foreach(args => (errorsFromParserTester _).tupled(args))

            "multiple parser errors occur" in new Test {
                val error = ErrorWrapper(Some(correlationId), Seq(BadRequestError, NinoFormatError))

                MockDeleteRequestDataParser
                  .parse(deleteRawData)
                  .returns(Left(error))

                val result: Future[Result] = controller.deleteRequest(nino, submissionId)(fakeRequest)

                status(result) shouldBe BAD_REQUEST
                contentAsJson(result) shouldBe Json.toJson(error)
                header("X-CorrelationId", result) shouldBe Some(correlationId)

                val auditResponse: AuditResponse = AuditResponse(BAD_REQUEST, Some(Seq(AuditError(BadRequestError.code), AuditError(NinoFormatError.code))), None)
                MockedAuditService.verifyAuditEvent(event(auditResponse, None)).once
            }

            "multiple errors occur for format errors" in new Test {
                val error = ErrorWrapper(
                    Some(correlationId),
                    Seq(
                        BadRequestError,
                        RuleSourceError)
                )

                MockDeleteRequestDataParser
                  .parse(deleteRawData)
                  .returns(Left(error))

                val result: Future[Result] = controller.deleteRequest(nino, submissionId)(fakeRequest)

                status(result) shouldBe BAD_REQUEST
                contentAsJson(result) shouldBe Json.toJson(error)
                header("X-CorrelationId", result) shouldBe Some(correlationId)

                val auditResponse: AuditResponse = AuditResponse(BAD_REQUEST, Some(
                    Seq(
                        AuditError(BadRequestError.code),
                        AuditError(RuleSourceError.code))),
                    None
                )

                MockedAuditService.verifyAuditEvent(event(auditResponse, None)).once
            }
        }

        "return downstream errors as per the spec" when {
            def serviceErrors(mtdError: MtdError, expectedStatus: Int) : Unit = {
                s"a ${mtdError.code} error is returned from the service" in new Test {

                    MockDeleteRequestDataParser
                      .parse(deleteRawData)
                      .returns(Right(deleteRequestData))

                    MockDeleteService
                      .deleteRequest(deleteRequestData)
                      .returns(Future.successful(Left(ErrorWrapper(Some(correlationId),Seq(mtdError)))))

                    val result: Future[Result] = controller.deleteRequest(nino, submissionId)(fakeRequest)

                    status(result) shouldBe expectedStatus
                    contentAsJson(result) shouldBe Json.toJson(mtdError)
                    header("X-CorrelationId", result) shouldBe Some(correlationId)

                    val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
                    MockedAuditService.verifyAuditEvent(event(auditResponse, Some(singleDeductionJson))).once
                }
            }
            val input = Seq(
                (NinoFormatError, BAD_REQUEST),
                (SubmissionIdFormatError, BAD_REQUEST),
                (NotFoundError, NOT_FOUND),
                (DownstreamError, INTERNAL_SERVER_ERROR),
            )
            input.foreach(args => (serviceErrors _).tupled(args))

        }
    }
}
