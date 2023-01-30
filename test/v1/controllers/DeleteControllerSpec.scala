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
import api.mocks.services.MockAuditService
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.mocks.requestParsers.MockDeleteRequestDataParser
import v1.mocks.services._
import v1.models.request.delete.{DeleteRawData, DeleteRequestData}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteRequestDataParser
    with MockDeleteService
    with MockAuditService {

  private val submissionId      = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val rawTaxYear        = "2022-23"
  private val taxYear           = TaxYear.fromMtd(rawTaxYear)
  private val deleteRawData     = DeleteRawData(nino, submissionId, Some(rawTaxYear))
  private val deleteRequestData = DeleteRequestData(Nino(nino), submissionId, Some(taxYear))

  "delete" should {
    "return a successful response with status 204 (No Content)" when {
      "a valid request is supplied" in new Test {

        MockDeleteRequestDataParser
          .parse(deleteRawData)
          .returns(Right(deleteRequestData))

        MockDeleteService
          .delete(deleteRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, None))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT)

      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {

        MockDeleteRequestDataParser
          .parse(deleteRawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {

        MockDeleteRequestDataParser
          .parse(deleteRawData)
          .returns(Right(deleteRequestData))

        MockDeleteService
          .delete(deleteRequestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, SubmissionIdFormatError))))

        runErrorTestWithAudit(SubmissionIdFormatError)

      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking {

    val controller = new DeleteController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRequestParser,
      service = mockDeleteService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.delete(nino, submissionId, Some(rawTaxYear))(fakeRequest)

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteCisDeductionsForSubcontractor",
        transactionName = "delete-cis-deductions-for-subcontractor",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "submissionId" -> submissionId, "taxYear" -> rawTaxYear),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
