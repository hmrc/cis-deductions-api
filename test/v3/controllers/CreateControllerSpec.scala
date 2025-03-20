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

package v3.controllers

import models.errors.RuleUnalignedDeductionsPeriodError
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas.Method._
import shared.hateoas._
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.Nino
import shared.models.errors.{ErrorWrapper, NinoFormatError}
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v3.controllers.validators.MockedCreateValidatorFactory
import v3.fixtures.AmendRequestFixtures.requestJson
import v3.fixtures.CreateRequestFixtures._
import v3.mocks.services.MockCreateService
import v3.models.request.create
import v3.models.response.create.{CreateHateoasData, CreateResponseModel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockSharedAppConfig
    with MockedCreateValidatorFactory
    with MockCreateService
    with MockHateoasFactory
    with MockAuditService {

  private val responseId  = "S4636A77V5KB8625U"
  private val requestData = create.CreateRequestData(Nino(validNino), parsedRequestData)

  val response: CreateResponseModel = CreateResponseModel(responseId)

  val testHateoasLinks: Seq[Link] = Seq(
    Link(
      href = s"/individuals/deductions/cis/$validNino/current-position",
      rel = "retrieve-cis-deductions-for-subcontractor",
      method = GET
    )
  )

  private val parsedHateoas = Json.parse(hateoasResponse(validNino, responseId))

  "create" should {
    "return a successful response with status 200 (OK)" when {
      "a valid request is supplied for a cis POST request" in new Test {

        willUseValidator(returningSuccess(requestData))

        MockCreateService
          .create(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, CreateHateoasData(validNino, requestData))
          .returns(HateoasWrapper(response, testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestJson),
          maybeExpectedResponseBody = Some(parsedHateoas),
          maybeAuditResponseBody = Some(parsedHateoas)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {

        willUseValidator(returning(NinoFormatError))
        runErrorTestWithAudit(NinoFormatError, Some(requestJson))
      }

      "the service returns an error" in new Test {

        willUseValidator(returningSuccess(requestData))

        MockCreateService
          .create(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleUnalignedDeductionsPeriodError))))

        runErrorTestWithAudit(RuleUnalignedDeductionsPeriodError, maybeAuditRequestBody = Some(requestJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new CreateController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockedCreateValidatorFactory,
      service = mockCreateService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.create(validNino)(fakePostRequest(requestJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateCisDeductionsForSubcontractor",
        transactionName = "create-cis-deductions-for-subcontractor",
        detail = GenericAuditDetail(
          versionNumber = apiVersion.name,
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> validNino),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
