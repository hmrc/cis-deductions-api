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

package v1.controllers.requestParsers

import api.models.domain.Nino
import api.models.errors._
import support.UnitSpec
import v1.mocks.validators.MockRetrieveValidator
import v1.models.request.retrieve.{RetrieveRawData, RetrieveRequestData}

class RetrieveRequestParserSpec extends UnitSpec {

  private val nino        = "AA123456A"
  private val invalidNino = "PLKL87654"

  private val fromDate = "2019-04-06"
  private val toDate   = "2020-04-05"

  private val validRawInput = RetrieveRawData(nino, Some(fromDate), Some(toDate), Some("all"))

  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test extends MockRetrieveValidator {
    lazy val parser = new RetrieveRequestParser(mockValidator)
  }

  "parser" should {
    "parse correctly" when {
      "given valid raw data" in new Test {
        MockValidator
          .validate(validRawInput)
          .returns(Nil)

        private val result = parser.parseRequest(validRawInput)
        result shouldBe Right(RetrieveRequestData(Nino(nino), fromDate, toDate, "all"))
      }

      "given valid raw data with no Source specified" in new Test {
        val inputData = validRawInput.copy(source = None)

        MockValidator
          .validate(inputData)
          .returns(Nil)

        val result: Either[ErrorWrapper, RetrieveRequestData] = parser.parseRequest(inputData)
        result shouldBe Right(RetrieveRequestData(Nino(nino), fromDate, toDate, "all"))
      }
    }

    "reject invalid input" when {
      "given an invalid NINO" in new Test {
        val inputData = validRawInput.copy(nino = invalidNino)

        MockValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        val result: Either[ErrorWrapper, RetrieveRequestData] = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given multiple invalid field values" in new Test {
        val inputData = validRawInput.copy(fromDate = Some("asdf"), toDate = Some("231k"))

        MockValidator
          .validate(inputData)
          .returns(List(FromDateFormatError, ToDateFormatError))

        val result: Either[ErrorWrapper, RetrieveRequestData] = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(FromDateFormatError, ToDateFormatError))))
      }

      "given an invalid source" in new Test {
        val inputData = validRawInput.copy(source = Some("wrong source"))

        MockValidator
          .validate(inputData)
          .returns(List(RuleSourceError))

        val result: Either[ErrorWrapper, RetrieveRequestData] = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(correlationId, RuleSourceError))
      }

      "toDate is earlier than fromDate" in new Test {
        val inputData = validRawInput.copy(fromDate = Some("2020-04-06"), toDate = Some("2019-04-05"))

        MockValidator
          .validate(inputData)
          .returns(List(RuleToDateBeforeFromDateError))

        private val result = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(correlationId, RuleToDateBeforeFromDateError))
      }
    }
  }

}
