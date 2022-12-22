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

package v1.controllers

import mocks.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.RetrieveJson._
import v1.fixtures.RetrieveModels._
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveRequestParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveService}
import v1.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.domain.{Nino, TaxYear}
import v1.models.errors._
import v1.models.hateoas.HateoasWrapper
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieve.{RetrieveRawData, RetrieveRequestData}
import v1.models.response.retrieve.RetrieveResponseModel._
import v1.models.response.retrieve.{CisDeductions, RetrieveHateoasData, RetrieveResponseModel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveRequestParser
    with MockRetrieveService
    with MockHateoasFactory
    with MockAppConfig
    with MockAuditService
    with MockIdGenerator {

  private val nino                            = "AA123456A"
  private val fromDate                        = "2019-04-06"
  private val toDate                          = "2020-04-05"
  private val taxYear                         = TaxYear.fromMtd("2019-20")
  private val sourceRaw                       = Some("customer")
  private val sourceAll                       = "all"
  private val correlationId                   = "X-123"
  private val retrieveRawData                 = RetrieveRawData(nino, Some(fromDate), Some(toDate), sourceRaw)
  private val retrieveRequestData             = RetrieveRequestData(Nino(nino), fromDate, toDate, sourceAll)
  private val optionalFieldMissingRawData     = RetrieveRawData(nino, Some(fromDate), Some(toDate), None)
  private val optionalFieldMissingRequestData = RetrieveRequestData(Nino(nino), fromDate, toDate, sourceAll)

  def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "RetrieveCisDeductionsForSubcontractor",
      transactionName = "retrieve-cis-deductions-for-subcontractor",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        nino,
        None,
        `X-CorrelationId` = correlationId,
        None,
        auditResponse
      )
    )

  "RetrieveCis" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {
        MockedAppConfig.apiGatewayContext.returns("individuals/deductions/cis").anyNumberOfTimes()
        MockedAppConfig.featureSwitches.returns(Configuration("tys-api.enabled" -> false)).anyNumberOfTimes()

        MockRetrieveDeductionRequestParser
          .parse(retrieveRawData)
          .returns(Right(retrieveRequestData))

        MockRetrieveService
          .retrieveCisDeductions(retrieveRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        val responseWithHateoas: HateoasWrapper[RetrieveResponseModel[HateoasWrapper[CisDeductions]]] = HateoasWrapper(
          RetrieveResponseModel(
            totalDeductionAmount = Some(12345.56),
            totalCostOfMaterials = Some(234234.33),
            totalGrossAmountPaid = Some(2342424.56),
            Seq(
              HateoasWrapper(
                cisDeductions,
                Seq(
                  deleteCisDeduction(mockAppConfig, nino, "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", None, isSelf = false),
                  amendCisDeduction(mockAppConfig, nino, "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", isSelf = false)
                )
              ))
          ),
          Seq(
            retrieveCisDeduction(mockAppConfig, nino, fromDate, toDate, sourceRaw, isSelf = true),
            createCisDeduction(mockAppConfig, nino, isSelf = false))
        )

        MockHateoasFactory
          .wrapList(response, RetrieveHateoasData(nino, fromDate, toDate, sourceRaw, taxYear, response))
          .returns(responseWithHateoas)

        val result: Future[Result] = controller.retrieveDeductions(nino, Some(fromDate), Some(toDate), sourceRaw)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe singleDeductionJsonHateoas(fromDate, toDate)
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(singleDeductionJsonHateoas(fromDate, toDate)))
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(singleDeductionJsonHateoas(fromDate, toDate)))).once()
      }

      "a valid request is supplied when an optional field is missing" in new Test {
        MockedAppConfig.apiGatewayContext.returns("individuals/deductions/cis").anyNumberOfTimes()
        MockedAppConfig.featureSwitches.returns(Configuration("tys-api.enabled" -> false)).anyNumberOfTimes()

        MockRetrieveDeductionRequestParser
          .parse(optionalFieldMissingRawData)
          .returns(Right(optionalFieldMissingRequestData))

        MockRetrieveService
          .retrieveCisDeductions(optionalFieldMissingRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        val responseWithHateoas: HateoasWrapper[RetrieveResponseModel[HateoasWrapper[CisDeductions]]] = HateoasWrapper(
          RetrieveResponseModel(
            totalDeductionAmount = Some(12345.56),
            totalCostOfMaterials = Some(234234.33),
            totalGrossAmountPaid = Some(2342424.56),
            Seq(HateoasWrapper(cisDeductionsMissingOptional, Seq()))
          ),
          Seq(
            retrieveCisDeduction(mockAppConfig, nino, fromDate, toDate, sourceRaw, isSelf = true),
            createCisDeduction(mockAppConfig, nino, isSelf = false))
        )

        MockHateoasFactory
          .wrapList(response, RetrieveHateoasData(nino, fromDate, toDate, None, taxYear, response))
          .returns(responseWithHateoas)

        val result: Future[Result] = controller.retrieveDeductions(nino, Some(fromDate), Some(toDate), None)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe singleDeductionJsonHateoasMissingOptionalField
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(singleDeductionJsonHateoasMissingOptionalField))
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(singleDeductionJsonHateoasMissingOptionalField))).once()
      }

      "a valid request where response submission id is missing" in new Test {

        MockedAppConfig.apiGatewayContext.returns("individuals/deductions/cis").anyNumberOfTimes()
        MockedAppConfig.featureSwitches.returns(Configuration("tys-api.enabled" -> false)).anyNumberOfTimes()

        MockRetrieveDeductionRequestParser
          .parse(retrieveRawData)
          .returns(Right(retrieveRequestData))

        MockRetrieveService
          .retrieveCisDeductions(retrieveRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseNoId))))

        val responseWithHateoas: HateoasWrapper[RetrieveResponseModel[HateoasWrapper[CisDeductions]]] = HateoasWrapper(
          RetrieveResponseModel(
            totalDeductionAmount = Some(12345.56),
            totalCostOfMaterials = Some(234234.33),
            totalGrossAmountPaid = Some(2342424.56),
            Seq(HateoasWrapper(cisDeductionsNoId, Seq()))
          ),
          Seq(
            retrieveCisDeduction(mockAppConfig, nino, fromDate, toDate, sourceRaw, isSelf = true),
            createCisDeduction(mockAppConfig, nino, isSelf = false))
        )

        MockHateoasFactory
          .wrapList(responseNoId, RetrieveHateoasData(nino, fromDate, toDate, sourceRaw, taxYear, responseNoId))
          .returns(responseWithHateoas)

        val result: Future[Result] = controller.retrieveDeductions(nino, Some(fromDate), Some(toDate), sourceRaw)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe singleDeductionJsonHateoasNoId
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(singleDeductionJsonHateoasNoId))
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(singleDeductionJsonHateoasNoId))).once()
      }
    }

    "return the error as per spec" when {
      def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
        s"a ${error.code} error is returned from the parser" in new Test {

          MockRetrieveDeductionRequestParser
            .parse(retrieveRawData)
            .returns(Left(ErrorWrapper(correlationId, error)))

          val result: Future[Result] = controller.retrieveDeductions(nino, Some(fromDate), Some(toDate), sourceRaw)(fakeGetRequest)

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
        (StandardDownstreamError, INTERNAL_SERVER_ERROR)
      )
      input.foreach(args => (errorsFromParserTester _).tupled(args))

      "multiple parser errors occur" in new Test {
        val error = ErrorWrapper(correlationId, BadRequestError, Some(Seq(BadRequestError, NinoFormatError)))

        MockRetrieveDeductionRequestParser
          .parse(retrieveRawData)
          .returns(Left(error))

        val result: Future[Result] = controller.retrieveDeductions(nino, Some(fromDate), Some(toDate), sourceRaw)(fakeGetRequest)

        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe Json.toJson(error)
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse =
          AuditResponse(BAD_REQUEST, Some(Seq(AuditError(BadRequestError.code), AuditError(NinoFormatError.code))), None)
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(singleDeductionJson(fromDate, toDate)))).once()
      }

      "multiple errors occur for format errors" in new Test {
        val error = ErrorWrapper(
          correlationId,
          BadRequestError,
          Some(
            Seq(
              BadRequestError,
              RuleDateRangeInvalidError,
              ToDateFormatError,
              FromDateFormatError,
              ToDateFormatError,
              RuleMissingToDateError,
              RuleMissingFromDateError,
              RuleSourceError,
              RuleDateRangeOutOfDate
            ))
        )

        MockRetrieveDeductionRequestParser
          .parse(retrieveRawData)
          .returns(Left(error))

        val result: Future[Result] = controller.retrieveDeductions(nino, Some(fromDate), Some(toDate), sourceRaw)(fakeGetRequest)

        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe Json.toJson(error)
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(
          BAD_REQUEST,
          Some(
            Seq(
              AuditError(BadRequestError.code),
              AuditError(RuleDateRangeInvalidError.code),
              AuditError(ToDateFormatError.code),
              AuditError(FromDateFormatError.code),
              AuditError(ToDateFormatError.code),
              AuditError(RuleMissingToDateError.code),
              AuditError(RuleMissingFromDateError.code),
              AuditError(RuleSourceError.code),
              AuditError(RuleDateRangeOutOfDate.code)
            )),
          None
        )
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(singleDeductionJson(fromDate, toDate)))).once()
      }
    }

    "return downstream errors as per the spec" when {
      def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
        s"a ${mtdError.code} error is returned from the service" in new Test {

          MockRetrieveDeductionRequestParser
            .parse(retrieveRawData)
            .returns(Right(retrieveRequestData))

          MockRetrieveService
            .retrieveCisDeductions(retrieveRequestData)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

          val result: Future[Result] = controller.retrieveDeductions(nino, Some(fromDate), Some(toDate), sourceRaw)(fakeGetRequest)

          status(result) shouldBe expectedStatus
          contentAsJson(result) shouldBe Json.toJson(mtdError)
          header("X-CorrelationId", result) shouldBe Some(correlationId)

          val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)

          MockedAuditService.verifyAuditEvent(event(auditResponse, Some(singleDeductionJson(fromDate, toDate)))).once()
        }
      }

      val errors = List(
        (NinoFormatError, BAD_REQUEST),
        (NotFoundError, NOT_FOUND),
        (StandardDownstreamError, INTERNAL_SERVER_ERROR),
        (FromDateFormatError, BAD_REQUEST),
        (ToDateFormatError, BAD_REQUEST),
        (RuleDateRangeOutOfDate, BAD_REQUEST)
      )

      val extraTysErrors = List(
        (RuleTaxYearRangeInvalidError, BAD_REQUEST),
        (RuleTaxYearNotSupportedError, BAD_REQUEST)
      )

      (errors ++ extraTysErrors).foreach(args => (serviceErrors _).tupled(args))
    }
  }

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

}
