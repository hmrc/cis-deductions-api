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

package v1.controllers.requestParsers.validators

import play.api.libs.json.Json
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.controllers.requestParsers.validators.validations.CreateRequestModelValidator
import v1.models.request._
import v1.fixtures.CreateRequestFixtures._
import v1.models.errors.{InvalidBodyTypeError, RuleIncorrectOrEmptyBodyError}

class CreateRequestModelValidatorSpec extends UnitSpec{

  val nino = "AA123456A"

  class SetUp {
    val validator = new CreateRequestModelValidator
  }

  "running validation" should {
    "return no errors" when {
      "all the fields are submitted in a request" in new SetUp {

        validator
          .validate(
            CreateRawData(nino, requestJson))
          .isEmpty shouldBe true
      }

      "an optional field is omitted in a request" in new SetUp {

        validator
          .validate(
            CreateRawData(nino, missingOptionalRequestJson))
          .isEmpty shouldBe true
      }
    }
    "return errors" when {
      "invalid body type error" in new SetUp {
        private val result = validator.validate(
          CreateRawData(nino, missingMandatoryFieldRequestJson)
        )
        result.length shouldBe 1
        result shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
    }
  }
}