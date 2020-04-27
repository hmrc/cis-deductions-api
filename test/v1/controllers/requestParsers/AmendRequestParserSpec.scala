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
import v1.mocks.validators.MockAmendValidator
import v1.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError}
import v1.models.request.AmendRawData
import v1.fixtures.AmendRequestFixtures._

class AmendRequestParserSpec extends UnitSpec {

  val nino = "AA123456A"
  val invalidNino = "PLKL87654"
  val id = "S4636A77V5KB8625U"

  trait Test extends MockAmendValidator {
    lazy val parser = new AmendRequestParser(mockValidator)
  }

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
    "Reject invalid input" when {
      "mandatory field is given invalid data" in new Test {
        val inputData = AmendRawData(nino, id, invalidRequestJson)

        MockValidator
          .validate(inputData)
          .returns(List(BadRequestError))

        private val result = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(None, List(BadRequestError)))
      }
      "Nino format is incorrect" in new Test {
        val inputData = AmendRawData(invalidNino, id, requestJson)

        MockValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        private val result = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(None, List(NinoFormatError)))
      }
      "Id format is incorrect" in new Test {
        val inputData = AmendRawData(nino, "id", requestJson)

        MockValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        private val result = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(None, List(NinoFormatError)))
      }
    }
  }
}
