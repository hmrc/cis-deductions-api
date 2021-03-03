/*
 * Copyright 2021 HM Revenue & Customs
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
import v1.mocks.validators.MockDeleteValidator
import v1.models.errors._
import v1.models.request.delete.{DeleteRawData, DeleteRequestData}

class DeleteRequestDataParserSpec extends UnitSpec {
  val nino = "AA123456B"
  val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val   inputData = DeleteRawData(nino, submissionId)

  trait Test extends MockDeleteValidator {
    lazy val parser = new DeleteRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid raw data is supplied" in new Test {
        MockDeleteValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe
          Right(DeleteRequestData(Nino(nino), submissionId))
      }
    }
    "return an error wrapper" when {
      "a single validation error occurs" in new Test {
        MockDeleteValidator.validate(inputData).returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }
    "multiple validation errors occur" in new Test {
      MockDeleteValidator.validate(inputData).returns(List(NinoFormatError, SubmissionIdFormatError))

      parser.parseRequest(inputData) shouldBe
        Left(ErrorWrapper(correlationId, BadRequestError ,Some(Seq(NinoFormatError, SubmissionIdFormatError))))
    }
  }
}