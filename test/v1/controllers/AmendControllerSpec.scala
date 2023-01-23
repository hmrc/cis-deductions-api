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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.mocks.MockIdGenerator
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.fixtures.AmendRequestFixtures._
import v1.mocks.requestParsers.MockAmendRequestParser
import v1.mocks.services.MockAmendService
import v1.models.request.amend.{AmendRawData, AmendRequestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendRequestParser
    with MockAmendService
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val submissionId  = "S4636A77V5KB8625U"
  private val correlationId = "X-123"
  private val taxYear       = TaxYear.fromIso("2019-07-05")

  private val rawData     = AmendRawData(nino, submissionId, requestJson)
  private val requestData = AmendRequestData(Nino(nino), submissionId, taxYear, amendRequestObj)

  private val rawMissingOptionalAmendRequestData = AmendRawData(nino, submissionId, missingOptionalRequestJson)
  private val missingOptionalAmendRequestData    = AmendRequestData(Nino(nino), submissionId, taxYear, amendMissingOptionalRequestObj)

  "amendCis" should {
    "return a successful response with status 204 (NO CONTENT)" when {
      "a valid request is supplied for a cis PUT request" in new Test {
        MockAmendRequestDataParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, None))))

        runOkTestWithAudit(
          expectedStatus = NO_CONTENT,
          maybeExpectedResponseBody = None,
          maybeAuditRequestBody = Some(requestJson),
          maybeAuditResponseBody = None
        )
      }

      "a valid request is supplied for a cis PUT request with optional fields missing" in new Test {
        MockAmendRequestDataParser
          .parse(rawMissingOptionalAmendRequestData)
          .returns(Right(missingOptionalAmendRequestData))

        MockAmendService
          .amend(missingOptionalAmendRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, None))))

        runOkTestWithAudit(
          expectedStatus = NO_CONTENT,
          maybeExpectedResponseBody = None,
          maybeAuditRequestBody = Some(requestJson),
          maybeAuditResponseBody = None
        )
      }
    }

    "return errors as per the spec" when {
      "the parser validation fails" in new Test {
        MockAmendRequestDataParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(expectedError = NinoFormatError, maybeAuditRequestBody = Some(requestJson))
      }

      "the service returns an error" in new Test {
        MockAmendRequestDataParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(expectedError = RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(requestJson))

      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking {

    val controller = new AmendController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockAmendRequestParser,
      service = mockAmendService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.amend(nino, submissionId)(fakePostRequest(requestJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AmendCisDeductionsForSubcontractor",
        transactionName = "amend-cis-deductions-for-subcontractor",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          pathParams = Map("nino" -> nino, "submissionId" -> submissionId),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
