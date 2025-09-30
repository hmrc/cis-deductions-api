/*
 * Copyright 2025 HM Revenue & Customs
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

package v2.controllers

import models.errors.SubmissionIdFormatError
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{ErrorWrapper, NinoFormatError}
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v2.controllers.validators.MockedDeleteValidatorFactory
import v2.mocks.services.MockDeleteService
import v2.models.domain.SubmissionId
import v2.models.request.delete.DeleteRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockedDeleteValidatorFactory
    with MockSharedAppConfig
    with MockDeleteService
    with MockAuditService {

  private val submissionId      = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val rawTaxYear        = "2022-23"
  private val taxYear           = TaxYear.fromMtd(rawTaxYear)
  private val deleteRequestData = DeleteRequestData(Nino(validNino), SubmissionId(submissionId), Some(taxYear))

  "delete" should {
    "return a successful response with status 204 (No Content)" when {
      "a valid request is supplied" in new Test {

        willUseValidator(returningSuccess(deleteRequestData))

        MockDeleteService
          .delete(deleteRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, None))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT)

      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {

        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {

        willUseValidator(returningSuccess(deleteRequestData))

        MockDeleteService
          .delete(deleteRequestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, SubmissionIdFormatError))))

        runErrorTestWithAudit(SubmissionIdFormatError)

      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: DeleteController = new DeleteController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockedDeleteValidatorFactory,
      service = mockDeleteService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.delete(validNino, submissionId, Some(rawTaxYear))(fakeRequest)

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteCisDeductionsForSubcontractor",
        transactionName = "delete-cis-deductions-for-subcontractor",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = apiVersion.name,
          params = Map("nino" -> validNino, "submissionId" -> submissionId, "taxYear" -> rawTaxYear),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
