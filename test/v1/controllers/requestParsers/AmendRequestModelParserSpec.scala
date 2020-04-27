package v1.controllers.requestParsers

import junit.framework.Test
import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v1.fixtures.CreateRequestFixtures.{requestJson, requestObj}
import v1.models.request.{AmendRawData, AmendRequestData}

class AmendRequestModelParserSpec extends UnitSpec{

  val nino = "AA123456A"
  val invalidNino = "PLKL87654"
  val id = ""

  "parser" should {
    "accept a valid input" when {
      "a cis deduction has been passed" in new Test {
        val inputData = AmendRawData(nino, id, requestJson)

        MockValidator
          .validate(inputData)
          .returns(Nil)

        private val result = parser.parseRequest(inputData)

      }

      "Missing option field has passed" in new Test {
        val inputData = AmendRawData(nino, id, requestJson)

        MockValidator
          .validate(inputData)
          .returns(Nil)

        private val result = parser.parseRequest(inputData)
      }
    }
  }
}
