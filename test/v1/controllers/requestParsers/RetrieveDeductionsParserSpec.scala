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

package v1.controllers.requestParsers

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v1.mocks.validators.MockRetrieveValidator
import v1.models.errors.{BadRequestError, ErrorWrapper, FromDateFormatError, NinoFormatError, RuleSourceError, RuleToDateBeforeFromDateError, ToDateFormatError}
import v1.models.request._

class RetrieveDeductionsParserSpec extends UnitSpec {

  val nino = "AA123456A"
  val invalidNino = "PLKL87654"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test extends MockRetrieveValidator {
    lazy val parser = new RetrieveRequestParser(mockValidator)
  }

  "parser" should {
    "accept a valid input" when {
      "a valid retrieve deduction request has been made" in new Test {
        val inputData = RetrieveRawData(nino, Some("2019-04-06"), Some("2020-04-05"), Some("all"))
        MockValidator
          .validate(inputData)
          .returns(Nil)

        private val result = parser.parseRequest(inputData)
        result shouldBe Right(RetrieveRequestData(Nino(nino), "2019-04-06", "2020-04-05", "all"))
      }

      "a valid retrieve deduction request has been made with the optional field returning none" in new Test {
        val inputData = RetrieveRawData(nino, Some("2019-04-06"), Some("2020-04-05"), None)
        MockValidator
          .validate(inputData)
          .returns(Nil)

        private val result = parser.parseRequest(inputData)
        result shouldBe Right(RetrieveRequestData(Nino(nino), "2019-04-06", "2020-04-05", "all"))
      }
    }

    "reject invalid input" when {
      "an invalid nino is given" in new Test {
        val inputData = RetrieveRawData(invalidNino, Some("2018-04-05"), Some("2019-04-06"), Some("customer"))
        MockValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        private val result = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(correlationId, List(NinoFormatError)))
      }

      "a mandatory field is given invalid data" in new Test {
        val inputData = RetrieveRawData(nino, Some("asdf"), Some("231k"), Some("all"))
          MockValidator
          .validate(inputData)
          .returns(List(FromDateFormatError, ToDateFormatError))

        private val result = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(correlationId, List(FromDateFormatError, ToDateFormatError)))
      }

      "an invalid source is given" in new Test {
        val inputData = RetrieveRawData(nino, Some("2019-04-06"), Some("2020-04-05"), Some("fruit source"))
        MockValidator
          .validate(inputData)
          .returns(List(RuleSourceError))

        private val result = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(correlationId, List(RuleSourceError)))
      }

      "the to date given is before the from date" in new Test {
        val inputData = RetrieveRawData(nino, Some("2020-04-06"), Some("2019-04-05"), Some("contractor"))

        MockValidator
          .validate(inputData)
          .returns(List(RuleToDateBeforeFromDateError))

        private val result = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(correlationId, List(RuleToDateBeforeFromDateError)))
      }
    }
  }
}
