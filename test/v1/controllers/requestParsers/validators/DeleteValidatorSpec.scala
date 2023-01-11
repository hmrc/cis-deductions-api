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

package v1.controllers.requestParsers.validators

import support.UnitSpec
import v1.models.errors._
import v1.models.request.delete.DeleteRawData

class DeleteValidatorSpec extends UnitSpec {

  private val validNino         = "AA123456A"
  private val validSubmissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val rawTaxYear        = "2023-24"

  val validator = new DeleteValidator()

  "running a delete validation" should {

    "return no errors" when {
      "given a valid request" in {
        validator.validate(DeleteRawData(validNino, validSubmissionId, Some(rawTaxYear))) shouldBe Nil
      }
    }

    "return a single error" when {
      "given an invalid nino" in {
        validator.validate(DeleteRawData("23456A", validSubmissionId, Some(rawTaxYear))) shouldBe List(NinoFormatError)
      }

      "given an invalid submission id" in {
        validator.validate(DeleteRawData(validNino, "contractor1", Some(rawTaxYear))) shouldBe List(SubmissionIdFormatError)
      }

      "given a pre-TYS taxYear param" in {
        val input  = DeleteRawData(validNino, validSubmissionId, Some("2021-22"))
        val result = validator.validate(input)
        result shouldBe List(InvalidTaxYearParameterError)
      }
    }

    "return multiple errors" when {
      "given multiple wrong fields" in {
        val result = validator.validate(DeleteRawData("2sbt3456A", "idcontract123", Some("bad-tax-year-format")))
        result shouldBe List(NinoFormatError, SubmissionIdFormatError, TaxYearFormatError)
      }
    }
  }

}
