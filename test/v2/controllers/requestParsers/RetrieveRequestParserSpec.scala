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

package v2.controllers.requestParsers

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import support.UnitSpec
import v2.mocks.validators.MockRetrieveValidator
import v2.models.request.retrieve.{RetrieveRawData, RetrieveRequestData}

class RetrieveRequestParserSpec extends UnitSpec {

  private val nino        = "AA123456A"
  private val invalidNino = "PLKL87654"

  private val taxYearRaw = "2019-20"
  private val taxYear    = TaxYear.fromMtd(taxYearRaw)

  private val validRawInput = RetrieveRawData(nino, taxYearRaw, "all")

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
        result shouldBe Right(RetrieveRequestData(Nino(nino), taxYear, "all"))
      }
    }

    "reject invalid input" when {
      "given an invalid NINO" in new Test {
        private val inputData = validRawInput.copy(nino = invalidNino)

        MockValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        val result: Either[ErrorWrapper, RetrieveRequestData] = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given multiple invalid field values" in new Test {
        private val inputData = validRawInput.copy(taxYear = "2018-ZZ", source = "BAD-SOURCE")

        MockValidator
          .validate(inputData)
          .returns(List(FromDateFormatError, ToDateFormatError))

        val result: Either[ErrorWrapper, RetrieveRequestData] = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(FromDateFormatError, ToDateFormatError))))
      }

      "given an invalid source" in new Test {
        private val inputData = validRawInput.copy(source = "wrong source")

        MockValidator
          .validate(inputData)
          .returns(List(RuleSourceInvalidError))

        val result: Either[ErrorWrapper, RetrieveRequestData] = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(correlationId, RuleSourceInvalidError))
      }

    }
  }

}
