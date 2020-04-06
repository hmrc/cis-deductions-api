package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import support.IntegrationBaseSpec
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.Json
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}
import v1.fixtures.ListJson.singleDeductionJson
import v1.models.errors.{MtdError, NinoFormatError}

// scalastyle:off

class ListControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456B"
    val correlationId = "X-123"
    val fromDate: Option[String] = Some("2019-04-06")
    val toDate: Option[String] = Some("2020-04-05")
    val source: Option[String] = Some("Customer")


    def uri: String = s"/deductions/cis/$nino/current-position"



    def desUrl: String = s"/cross-regime/deductions-placeholder/CIS/$nino"

    def setupStubs(): StubMapping

    def request: WSRequest = {

      val queryParams = Seq("fromDate" -> fromDate, "toDate" -> toDate, "source" -> source)
        .collect {
          case (k, Some(v)) => (k, v)
        }
      setupStubs()
      buildRequest(uri)
        .addQueryStringParameters(queryParams: _*)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

  }


  "Calling the list Cis endpoint" should {

    "return a valid response with status OK" when {

      "valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, nino,fromDate, toDate,source)
        }

        val response: WSResponse = await(request.get)

        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe singleDeductionJson
      }

      "return error according to spec" when {

        def validationErrorTest(requestNino: String, requestFromDate: Option[String],
                                requestToDate: Option[String], requestSource: Option[String],
                                expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val fromDate: Option[String] = requestFromDate
            override val toDate: Option[String] = requestToDate
            override val source: Option[String] = requestSource


            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request.get)
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1234", Some("2019-04-06"), Some("2020-04-05"), Some("Customer"), BAD_REQUEST, NinoFormatError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

    }
  }
}
