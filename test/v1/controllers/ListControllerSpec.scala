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
import v1.mocks.requestParsers.MockListDeductionRequestParser
import v1.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.request._
import v1.models.responseData.listDeductions.{DeductionsDetails, ListResponseModel, PeriodDeductions}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockListService, _}
import v1.fixtures.ListJson._
import v1.mocks.hateoas.MockHateoasFactory
import v1.models.errors._
import v1.models.hateoas.Link
import v1.models.hateoas.Method.GET
import v1.models.outcomes.ResponseWrapper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListControllerSpec extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockListDeductionRequestParser
    with MockListService
    with MockHateoasFactory
    with MockAppConfig
    with MockAuditService {

    trait Test {
        val hc = HeaderCarrier()

        val controller = new ListController(
            authService = mockEnrolmentsAuthService,
            lookupService = mockMtdIdLookupService,
            requestParser = mockRequestParser,
            service = mockService,
            hateoasFactory = mockHateoasFactory,
            auditService = mockAuditService,
            cc = cc
        )

        MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
        MockedEnrolmentsAuthService.authoriseUser()

    }

    private val nino = "AA123456A"
    private val fromDate = Some("2019-04-06")
    private val toDate = Some("2020-04-05")
    private val source = Some("customer")
    private val correlationId = "X-123"
    private val listRawData = ListDeductionsRawData(nino,fromDate, toDate, source)
    private val listRequestData = ListDeductionsRequest(Nino(nino), fromDate.get, toDate.get, source)
    private val optionalFieldMissingRawData = ListDeductionsRawData(nino, fromDate, toDate, None)
    private val optionalFieldMissingRequestData = ListDeductionsRequest(Nino(nino), fromDate.get, toDate.get, None)

    val response: ListResponseModel[DeductionsDetails] =
        ListResponseModel(
            Seq(DeductionsDetails(
                submissionId = Some("54759eb3c090d83494e2d804"),
                fromDate = "2019-04-06",
                toDate = "2020-04-05",
                contractorName = "Bovis",
                employerRef = "BV40092",
                Seq(
                    PeriodDeductions(
                        deductionAmount = 355.00,
                        deductionFromDate = "2019-06-06",
                        deductionToDate = "2019-07-05",
                        costOfMaterials = Some(35.00),
                        grossAmountPaid = 1457.00,
                        submissionDate = "2020-01-14",
                        submittedBy = "customer"),
                    PeriodDeductions(
                        deductionAmount = 355.00,
                        deductionFromDate = "2019-07-06",
                        deductionToDate = "2019-08-05",
                        costOfMaterials = Some(35.00),
                        grossAmountPaid = 1457.00,
                        submissionDate = "2020-01-14",
                        submittedBy = "customer"
                    )
                )
            )
            )
        )

    val testHatoeasLinks: Seq[Link] = Seq(
        Link(
            href = s"/deductions/cis/$nino/current-position",
            rel = "list-cis-deductions-for-subcontractor",
            method = GET
        )
    )

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
        AuditEvent(
            auditType = "listCisDeductionsAuditType",
            transactionName = "list-cis-deductions-transaction-type",
            detail = GenericAuditDetail(
                userType = "Individual",
                agentReferenceNumber = None,
                nino,
                `X-CorrelationId` = correlationId,
                auditResponse
            )
        )

    "ListCis" should {
        "return a successful response with status 200 (OK)" when {
            "a valid request is supplied for a cis get request" in new Test {
                MockListDeductionRequestParser
                  .parse(listRawData)
                  .returns(Right(listRequestData))

                MockListService
                  .listCisDeductions(listRequestData)
                  .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

                val result: Future[Result] = controller.listDeductions(nino, fromDate, toDate, source)(fakeGetRequest)

                status(result) shouldBe OK
                contentAsJson(result) shouldBe singleDeductionJson
                header("X-CorrelationId", result) shouldBe Some(correlationId)

                val auditResponse: AuditResponse = AuditResponse(OK, None, Some(singleDeductionJson))
                MockedAuditService.verifyAuditEvent(event(auditResponse, Some(singleDeductionJson))).once()
            }

            "a valid request is supplied when an optional field is missing" in new Test {

                MockListDeductionRequestParser
                  .parse(optionalFieldMissingRawData)
                  .returns(Right(optionalFieldMissingRequestData))

                MockListService
                  .listCisDeductions(optionalFieldMissingRequestData)
                  .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

                val result: Future[Result] = controller.listDeductions(nino,fromDate,toDate,None)(fakeGetRequest)

                status(result) shouldBe OK
                contentAsJson(result) shouldBe singleDeductionJson
                header("X-CorrelationId", result) shouldBe Some(correlationId)

                val auditResponse: AuditResponse = AuditResponse(OK, None, Some(singleDeductionJson))
                MockedAuditService.verifyAuditEvent(event(auditResponse, Some(singleDeductionJson))).once()
            }



        }

        "return the error as per spec" when {
            def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
                s"a ${error.code} error is returned from the parser" in new Test {

                    MockListDeductionRequestParser
                      .parse(listRawData)
                      .returns(Left(ErrorWrapper(Some(correlationId), Seq(error))))

                    val result: Future[Result] = controller.listDeductions(nino, fromDate, toDate, source)(fakeGetRequest)


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

                MockListDeductionRequestParser
                  .parse(listRawData)
                  .returns(Left(error))

                val result: Future[Result] = controller.listDeductions(nino, fromDate, toDate, source)(fakeGetRequest)

                status(result) shouldBe BAD_REQUEST
                contentAsJson(result) shouldBe Json.toJson(error)
                header("X-CorrelationId", result) shouldBe Some(correlationId)

                val auditResponse: AuditResponse = AuditResponse(BAD_REQUEST, Some(Seq(AuditError(BadRequestError.code), AuditError(NinoFormatError.code))), None)
                MockedAuditService.verifyAuditEvent(event(auditResponse, Some(singleDeductionJson))).once
            }

            "multiple errors occur for format errors" in new Test {
                val error = ErrorWrapper(
                    Some(correlationId),
                    Seq(
                        BadRequestError,
                        RuleDateRangeInvalidError,
                        ToDateFormatError,
                        FromDateFormatError,
                        ToDateFormatError,
                        RuleMissingToDateError,
                        RuleMissingFromDateError,
                        RuleSourceError)
                )

                MockListDeductionRequestParser
                  .parse(listRawData)
                  .returns(Left(error))

                val result: Future[Result] = controller.listDeductions(nino,fromDate,toDate,source)(fakeGetRequest)

                status(result) shouldBe BAD_REQUEST
                contentAsJson(result) shouldBe Json.toJson(error)
                header("X-CorrelationId", result) shouldBe Some(correlationId)

                val auditResponse: AuditResponse = AuditResponse(BAD_REQUEST, Some(
                    Seq(
                        AuditError(BadRequestError.code),
                        AuditError(RuleDateRangeInvalidError.code),
                        AuditError(ToDateFormatError.code),
                        AuditError(FromDateFormatError.code),
                        AuditError(ToDateFormatError.code),
                        AuditError(RuleMissingToDateError.code),
                        AuditError(RuleMissingFromDateError.code),
                        AuditError(RuleSourceError.code))),
                    None
                )

                MockedAuditService.verifyAuditEvent(event(auditResponse, Some(singleDeductionJson))).once
            }
        }

        "return downstream errors as per the spec" when {
            def serviceErrors(mtdError: MtdError, expectedStatus: Int) : Unit = {
                s"a ${mtdError.code} error is returned from the service" in new Test {

                    MockListDeductionRequestParser
                      .parse(listRawData)
                      .returns(Right(listRequestData))

                    MockListService
                      .listCisDeductions(listRequestData)
                      .returns(Future.successful(Left(ErrorWrapper(Some(correlationId),Seq(mtdError)))))

                    val result: Future[Result] = controller.listDeductions(nino,fromDate,toDate,source)(fakeGetRequest)

                    status(result) shouldBe expectedStatus
                    contentAsJson(result) shouldBe Json.toJson(mtdError)
                    header("X-CorrelationId", result) shouldBe Some(correlationId)

                    val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
                    MockedAuditService.verifyAuditEvent(event(auditResponse, Some(singleDeductionJson))).once
                }
            }
            val input = Seq(
                (NinoFormatError, BAD_REQUEST),
                (NotFoundError, NOT_FOUND),
                (DownstreamError, INTERNAL_SERVER_ERROR),
                (FromDateFormatError, BAD_REQUEST),
                (ToDateFormatError, BAD_REQUEST),
            )
            input.foreach(args => (serviceErrors _).tupled(args))

        }
    }
}
