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

import models.domain.CisSource
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas._
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v2.controllers.validators.MockedRetrieveValidatorFactory
import v2.fixtures.RetrieveJson._
import v2.fixtures.RetrieveModels._
import v2.mocks.services.MockRetrieveService
import v2.models.request.retrieve.RetrieveRequestData
import v2.models.response.retrieve.RetrieveResponseModel._
import v2.models.response.retrieve.{CisDeductions, RetrieveHateoasData, RetrieveResponseModel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockedRetrieveValidatorFactory
    with MockRetrieveService
    with MockHateoasFactory
    with MockSharedAppConfig
    with MockAuditService {

  private val fromDate = "2019-04-06"
  private val toDate   = "2020-04-05"

  private val taxYearRaw          = "2019-20"
  private val taxYear             = TaxYear.fromMtd(taxYearRaw)
  private val sourceRaw           = CisSource.`customer`
  private val retrieveRequestData = RetrieveRequestData(Nino(validNino), taxYear, sourceRaw)

  "retrieve" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {

        MockedSharedAppConfig.apiGatewayContext.returns("individuals/deductions/cis").anyNumberOfTimes()
        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("tys-api.enabled" -> false)).anyNumberOfTimes()

        val responseWithHateoas: HateoasWrapper[RetrieveResponseModel[HateoasWrapper[CisDeductions]]] = HateoasWrapper(
          RetrieveResponseModel(
            totalDeductionAmount = Some(12345.56),
            totalCostOfMaterials = Some(234234.33),
            totalGrossAmountPaid = Some(2342424.56),
            Seq(
              HateoasWrapper(
                cisDeductions,
                Seq(
                  deleteCisDeduction(mockSharedAppConfig, validNino, "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", None, isSelf = false),
                  amendCisDeduction(mockSharedAppConfig, validNino, "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", isSelf = false)
                )
              ))
          ),
          Seq(
            retrieveCisDeduction(mockSharedAppConfig, validNino, taxYear, sourceRaw.toString, isSelf = true),
            createCisDeduction(mockSharedAppConfig, validNino, isSelf = false)
          )
        )

        willUseValidator(returningSuccess(retrieveRequestData))

        MockRetrieveService
          .retrieve(retrieveRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = None,
          maybeExpectedResponseBody = Some(singleDeductionJsonHateoas(fromDate, toDate, taxYearRaw)),
          maybeAuditResponseBody = Some(singleDeductionJsonHateoas(fromDate, toDate, taxYearRaw))
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {

        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {

        willUseValidator(returningSuccess(retrieveRequestData))

        MockRetrieveService
          .retrieve(retrieveRequestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, InternalError))))

        runErrorTestWithAudit(InternalError)

      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: RetrieveController = new RetrieveController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockedRetrieveValidatorFactory,
      service = mockRetrieveService,
      hateoasFactory = new HateoasFactory(mockSharedAppConfig),
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.retrieve(validNino, taxYearRaw, sourceRaw.toString)(fakeRequest)

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "RetrieveCisDeductionsForSubcontractor",
        transactionName = "retrieve-cis-deductions-for-subcontractor",
        detail = GenericAuditDetail(
          versionNumber = apiVersion.name,
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "taxYear" -> taxYearRaw, "source" -> sourceRaw.toString),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
