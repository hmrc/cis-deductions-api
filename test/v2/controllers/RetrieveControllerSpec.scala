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

package v2.controllers

import api.hateoas.{HateoasWrapper, MockHateoasFactory}
import api.mocks.MockAppConfig
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import config.AppConfig
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{Nino, Source, TaxYear}
import shared.models.errors._
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
    with MockAppConfig
    with MockAuditService {

  private implicit val appConfig: AppConfig = mockAppConfig
  private val fromDate                      = "2019-04-06"
  private val toDate                        = "2020-04-05"

  private val taxYearRaw          = "2019-20"
  private val taxYear             = TaxYear.fromMtd(taxYearRaw)
  private val sourceRaw           = Source.`customer`
  private val retrieveRequestData = RetrieveRequestData(Nino(nino), taxYear, sourceRaw)

  "retrieve" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {

        MockedAppConfig.apiGatewayContext.returns("individuals/deductions/cis").anyNumberOfTimes()
        MockedAppConfig.featureSwitches.returns(Configuration("tys-api.enabled" -> false)).anyNumberOfTimes()

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
            retrieveCisDeduction(mockAppConfig, nino, taxYear, sourceRaw.toString, isSelf = true),
            createCisDeduction(mockAppConfig, nino, isSelf = false))
        )

        willUseValidator(returningSuccess(retrieveRequestData))

        MockRetrieveService
          .retrieve(retrieveRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrapList(response, RetrieveHateoasData(nino, taxYear, sourceRaw.toString))
          .returns(responseWithHateoas)

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

  trait Test extends ControllerTest with AuditEventChecking {

    val controller = new RetrieveController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockedRetrieveValidatorFactory,
      service = mockRetrieveService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieve(nino, taxYearRaw, sourceRaw.toString)(fakeRequest)

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "RetrieveCisDeductionsForSubcontractor",
        transactionName = "retrieve-cis-deductions-for-subcontractor",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYearRaw, "source" -> sourceRaw.toString),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
