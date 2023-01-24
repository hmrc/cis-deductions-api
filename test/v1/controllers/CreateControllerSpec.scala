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
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.MockAuditService
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas.Method._
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.fixtures.AmendRequestFixtures.requestJson
import v1.fixtures.CreateRequestFixtures._
import v1.mocks.requestParsers.MockCreateRequestParser
import v1.mocks.services._
import v1.models.request.create.{CreateRawData, CreateRequestData}
import v1.models.response.create.{CreateHateoasData, CreateResponseModel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateRequestParser
    with MockCreateService
    with MockHateoasFactory
    with MockAppConfig
    with MockAuditService {

  private val responseId  = "S4636A77V5KB8625U"
  private val rawData     = CreateRawData(nino, requestJson)
  private val requestData = CreateRequestData(Nino(nino), requestObj)

  val response: CreateResponseModel = CreateResponseModel(responseId)

  val testHateoasLinks: Seq[Link] = Seq(
    Link(
      href = s"/individuals/deductions/cis/$nino/current-position",
      rel = "retrieve-cis-deductions-for-subcontractor",
      method = GET
    )
  )

  private val parsedHateoas = Json.parse(hateoasResponse(nino, responseId))

  "create" should {
    "return a successful response with status 200 (OK)" when {
      "a valid request is supplied for a cis POST request" in new Test {

        MockCreateRequestDataParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateService
          .create(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, CreateHateoasData(nino, requestData))
          .returns(HateoasWrapper(response, testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestJson),
          maybeExpectedResponseBody = Some(parsedHateoas),
          maybeAuditResponseBody = None
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {

        MockCreateRequestDataParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError, Some(requestJson))
      }

      "the service returns an error" in new Test {

        MockCreateRequestDataParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateService
          .create(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleUnalignedDeductionsPeriodError))))

        runErrorTestWithAudit(RuleUnalignedDeductionsPeriodError, maybeAuditRequestBody = Some(requestJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking {

    MockedAppConfig.featureSwitches.returns(Configuration("tys-api.enabled" -> false)).anyNumberOfTimes()

    val controller = new CreateController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRequestDataParser,
      service = mockCreateService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      appConfig = mockAppConfig,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.create(nino)(fakePostRequest(requestJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateCisDeductionsForSubcontractor",
        transactionName = "create-cis-deductions-for-subcontractor",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          pathParams = Map("nino" -> nino),
          queryParams = None,
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
