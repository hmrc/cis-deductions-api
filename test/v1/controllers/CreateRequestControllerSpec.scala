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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers._
import v1.mocks.services.{MockEnrolmentsAuthService, _}
import v1.models.audit._
import v1.models.hateoas.Method.POST
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.request._
import v1.fixtures.CreateRequestFixtures._
import v1.models.outcomes.ResponseWrapper
import v1.models.responseData.{CreateHateoasData, CreateResponseModel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateRequestControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateRequestParser
    with MockCreateService
    with MockHateoasFactory
    with MockAuditService {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new CreateRequestController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRequestDataParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()

  }

  private val nino = "AA123456A"
  private val correlationId = "X-123"

  private val responseId = "S4636A77V5KB8625U"

  private val rawCreateRequest = CreateRawData(nino, requestJson)
  private val createRequest = CreateRequestData(Nino(nino), requestObj)

  private val rawMissingOptionalCreateRequest = CreateRawData(nino, missingOptionalRequestJson)
  private val missingOptionalCreateRequest = CreateRequestData(Nino(nino), missingOptionalRequestObj)

  val response = CreateResponseModel(responseId)

  val testHateoasLinks: Seq[Link] = Seq(
    Link(
      href = s"/deductions/cis/$nino/amendments",
      method = POST,
      rel = "self"
    )
  )

  def event(auditResponse: CreateAuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "createRequest",
      transactionName = "create-request",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("nino" -> nino),
        requestBody = requestBody,
        `X-CorrelationId` = correlationId,
        auditResponse = auditResponse
      )
    )

  "createRequest" should {
    "return a successful hateoas response with status 200 (OK)" when {
      "a valid request is supplied for a cis post request" in new Test {

        MockCreateRequestDataParser
          .parse(rawCreateRequest)
          .returns(Right(createRequest))

        MockCreateService
          .submitCreateRequest(createRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, CreateHateoasData(nino))
          .returns(HateoasWrapper(response, testHateoasLinks))

        val result: Future[Result] = controller.createRequest(nino)(fakePostRequest(Json.toJson(requestJson)))

        status(result) shouldBe CREATED
        contentAsJson(result) shouldBe Json.parse(hateoasResponse(nino, responseId))
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: CreateAuditResponse = CreateAuditResponse(CREATED, None, Some(Json.parse(hateoasResponse(nino, responseId))))
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(requestJson))).once
      }

      "a valid request is supplied when an optional field is missing" in new Test {

        MockCreateRequestDataParser
          .parse(rawMissingOptionalCreateRequest)
          .returns(Right(missingOptionalCreateRequest))

        MockCreateService
          .submitCreateRequest(missingOptionalCreateRequest)
          .returns(Future.successful((Right(ResponseWrapper(correlationId, response)))))

        MockHateoasFactory
          .wrap(response, CreateHateoasData(nino))
          .returns(HateoasWrapper(response, testHateoasLinks))

        val result: Future[Result] = controller.createRequest(nino)(fakePostRequest(Json.toJson(missingOptionalRequestJson)))

        status(result) shouldBe CREATED
        contentAsJson(result) shouldBe Json.parse(hateoasResponse(nino, responseId))
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: CreateAuditResponse = CreateAuditResponse(CREATED, None, Some(Json.parse(hateoasResponse(nino, responseId))))
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(missingOptionalRequestJson))).once
      }
    }
  }
}
