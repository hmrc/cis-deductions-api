package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import data.AmendDataExamples._
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class AmendControllerISpec extends IntegrationBaseSpec{
  private trait Test {
    val nino = "AA123456A"
    val id = "S4636A77V5KB8625U"


    def uri: String = s"/deductions/cis/$nino/amendments/$id"
    def desUri: String = s"/cross-regime/deductions-placeholder/CIS/$nino/amendments/$id"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the amend endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.mockDes(DesStub.PUT, desUri, Status.OK, deductionsResponseBody, None)
        }
        val response: WSResponse = await(request().put(Json.parse(requestJson)))
        response.status shouldBe Status.OK
        response.json shouldBe deductionsResponseBody
      }
    }
    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestId: String, body: JsValue, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val id: String = requestId

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(body))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A","S4636A77V5KB8625U", requestBodyJson, Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A","ID-SUB", requestBodyJson, Status.BAD_REQUEST, DeductionIdFormatError),
          ("AA123456A", "S4636A77V5KB8625U", Json.parse("""{}"""), Status.BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A","S4636A77V5KB8625U", requestBodyJsonErrorFromDate, Status.BAD_REQUEST, FromDateFormatError),
          ("AA123456A","S4636A77V5KB8625U", requestBodyJsonErrorToDate, Status.BAD_REQUEST, ToDateFormatError),
          ("AA123456A","S4636A77V5KB8625U", requestBodyJsonErrorDeductionToDate, Status.BAD_REQUEST, DeductionToDateFormatError),
          ("AA123456A","S4636A77V5KB8625U", requestBodyJsonErrorDeductionFromDate, Status.BAD_REQUEST, DeductionFromDateFormatError),
          ("AA123456A","S4636A77V5KB8625U", requestBodyJsonErrorToDateInvalid, Status.BAD_REQUEST, RuleToDateError),
          ("AA123456A","S4636A77V5KB8625U", requestBodyJsonErrorFromDateInvalid, Status.BAD_REQUEST, RuleFromDateError),
          ("AA123456A","S4636A77V5KB8625U", requestBodyJsonErrorDateRangeInvalid, Status.BAD_REQUEST, RuleDateRangeInvalidError),
          ("AA123456A","S4636A77V5KB8625U", requestBodyJsonErrorRuleCostOfMaterial, Status.BAD_REQUEST, RuleCostOfMaterialsError),
          ("AA123456A","S4636A77V5KB8625U", requestBodyJsonErrorRuleGrossAmountPaid, Status.BAD_REQUEST, RuleGrossAmountError),
          ("AA123456A","S4636A77V5KB8625U", requestBodyJsonErrorRuleDeductionAmount, Status.BAD_REQUEST, RuleDeductionAmountError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new Test {
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.mockDes(DesStub.PUT, desUri, desStatus, Json.parse(errorBody(desCode)), None)
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.NOT_FOUND, "NOT_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INVALID_IDVALUE", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_DEDUCTION_DATE_FROM", Status.BAD_REQUEST, DeductionFromDateFormatError),
          (Status.BAD_REQUEST, "INVALID_DEDUCTION_DATE_TO", Status.BAD_REQUEST, DeductionToDateFormatError),
          (Status.BAD_REQUEST, "INVALID_DATE_FROM", Status.BAD_REQUEST, FromDateFormatError),
          (Status.BAD_REQUEST, "INVALID_DATE_TO", Status.BAD_REQUEST, ToDateFormatError),
          (Status.BAD_REQUEST, "INVALID_DEDUCTIONS_DATE_RANGE", Status.BAD_REQUEST, RuleDeductionsDateRangeInvalidError),
          (Status.BAD_REQUEST, "INVALID_DEDUCTIONS_TO_DATE_BEFORE_DEDUCTIONS_FROM_DATE", Status.BAD_REQUEST, RuleToDateBeforeFromDateError),
          (Status.FORBIDDEN, "INVALID_NO_CHANGE", Status.FORBIDDEN, RuleNoChangeError)

        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}
