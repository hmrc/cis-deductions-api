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
import api.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError}
import support.UnitSpec
import v1.fixtures.CreateRequestFixtures._
import v1.mocks.validators.MockCreateValidator
import v1.models.request.create.{CreateRawData, CreateRequestData}

class CreateRequestParserSpec extends UnitSpec {

  val nino                           = "AA123456A"
  val invalidNino                    = "PLKL87654"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test extends MockCreateValidator {
    lazy val parser = new CreateRequestParser(mockValidator)
  }

  "parser" should {
    "accept a valid input" when {
      "a cis deduction has been passed" in new Test {
        val inputData = CreateRawData(nino, requestJson)

        MockValidator
          .validate(inputData)
          .returns(Nil)

        private val result = parser.parseRequest(inputData)
        result shouldBe Right(CreateRequestData(Nino(nino), requestObj))

      }

      "Missing option field has passed" in new Test {
        val inputData = CreateRawData(nino, missingOptionalRequestJson)

        MockValidator
          .validate(inputData)
          .returns(Nil)

        private val result = parser.parseRequest(inputData)
        result shouldBe Right(CreateRequestData(Nino(nino), missingOptionalRequestObj))
      }
    }
    "Reject invalid input" when {
      "mandatory field is given invalid data" in new Test {
        val inputData = CreateRawData(nino, invalidRequestJson)

        MockValidator
          .validate(inputData)
          .returns(List(BadRequestError))

        private val result = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError))
      }
      "Nino format is incorrect" in new Test {
        val inputData = CreateRawData(nino, requestJson)

        MockValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        private val result = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }
  }

}
