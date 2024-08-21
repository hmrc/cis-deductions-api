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

import play.api.Configuration
import shared.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.config.MockAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import shared.services.MockAuditService
import v1.controllers.validators.MockedAmendValidatorFactory
import v1.fixtures.AmendRequestFixtures._
import v1.mocks.services.MockAmendService
import v1.models.domain.SubmissionId
import v1.models.request.amend.AmendRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockedAmendValidatorFactory
    with MockAmendService
    with MockAuditService {

  private val submissionId = "S4636A77V5KB8625U"
  private val taxYear      = TaxYear.fromIso("2019-07-05")
  private val requestData  = AmendRequestData(Nino(validNino), SubmissionId(submissionId), taxYear, amendRequestObj)

  "amend" should {
    "return a successful response with status 204 (NO CONTENT)" when {
      "a valid request is supplied for a cis PUT request" in new Test {

        willUseValidator(returningSuccess(requestData))
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
    }

    "return errors as per the spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(expectedError = NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))
        MockAmendService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(expectedError = RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(requestJson))

      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new AmendController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockedAmendValidatorFactory,
      service = mockAmendService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.amend(validNino, submissionId)(fakePostRequest(requestJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AmendCisDeductionsForSubcontractor",
        transactionName = "amend-cis-deductions-for-subcontractor",
        detail = GenericAuditDetail(
          versionNumber = apiVersion.name,
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "submissionId" -> submissionId),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
