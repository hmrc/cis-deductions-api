package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import support.IntegrationBaseSpec
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.http.HeaderNames._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}
import v1.fixtures.RetrieveJson._
import v1.models.errors._

class RetrieveControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"
    val correlationId = "X-123"
    val fromDate = "2019-04-06"
    val toDate = "2020-04-05"
    val source = "customer"
    val queryParams = Seq("fromDate" -> fromDate, "toDate" -> toDate, "source" -> source)

    def uri: String = s"/individuals/deductions/cis/$nino/current-position"

    def desUrl: String = s"/income-tax/cis/deductions/$nino/current-position"

    def setupStubs(): StubMapping

    def request: WSRequest = {

      setupStubs()
      buildRequest(uri)
        .withQueryStringParameters(queryParams: _*)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the retrieve endpoint" should {

    "return a valid response with status OK" when {

      "valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.mockDes(DesStub.GET, desUrl, OK, singleDeductionJson, Some(queryParams))
        }

        val response: WSResponse = await(request.get)

        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe singleDeductionJsonHateoas
      }

      "return error according to spec" when {

        def validationErrorTest(requestNino: String, requestFromDate: String,
                                requestToDate: String, requestSource: String,
                                expectedStatus: Int, expectedBody: MtdError, qParams: Option[Seq[(String, String)]]): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val fromDate: String = requestFromDate
            override val toDate: String = requestToDate
            override val source: String = requestSource
            override val queryParams = if(qParams.isDefined) qParams.get else Seq("fromDate" -> fromDate, "toDate" -> toDate, "source" -> source)

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
          ("AA12345", "2019-04-06", "2020-04-05", "customer", BAD_REQUEST, NinoFormatError, None),
          ("AA123456B", "2019-04", "2020-04-05", "customer", BAD_REQUEST, FromDateFormatError, None),
          ("AA123456B", "2019-04-06", "2020-04", "customer", BAD_REQUEST, ToDateFormatError, None),
          ("AA123456B", "2019-04-06", "2020-04-05", "asdf", BAD_REQUEST, RuleSourceError, None),
          ("AA123456B", "2020-04-05", "2019-04-06", "customer", BAD_REQUEST, RuleDateRangeInvalidError, None),
          ("AA123456B", "2019-04-06", "2020-04-05", "customer", BAD_REQUEST, RuleMissingToDateError, Some(Seq("fromDate" -> "2019-04-06", "source" -> "all"))),
          ("AA123456B", "2019-04-06", "2020-04-05", "customer", BAD_REQUEST, RuleMissingFromDateError, Some(Seq("toDate" -> "2020-04-05", "source" -> "all")))
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }
    }

    "des service error" when {

      def errorBody(code: String): JsValue =
        Json.parse(
          s"""{
             |  "code": "$code",
             |  "reason": "des message"
             |}""".stripMargin)

      def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"des returns an $desCode error and status $desStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.mockDes(DesStub.GET, desUrl, desStatus, errorBody(desCode), None)
          }

          val response: WSResponse = await(request.get)
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        (BAD_REQUEST, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_REQUEST", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_PERIOD_START", BAD_REQUEST, FromDateFormatError),
        (BAD_REQUEST, "INVALID_PERIOD_END", BAD_REQUEST, ToDateFormatError),
        (UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE",FORBIDDEN,RuleDateRangeOutOfDate)
      )
      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }
}
