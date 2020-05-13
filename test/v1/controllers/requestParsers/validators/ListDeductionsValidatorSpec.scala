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

import support.UnitSpec
import v1.models.errors._
import v1.models.request.ListDeductionsRawData

class ListDeductionsValidatorSpec extends UnitSpec {

  private val nino = "AA123456A"
  private val invalidNino = "GHFG197854"

  class SetUp {
    val validator = new ListDeductionsValidator
  }

  "running validation" should {
    "return no errors" when {
      "all query parameters are passed in the request" in new SetUp {
        validator
          .validate(ListDeductionsRawData(nino, Some("2019-04-06"), Some("2020-04-05"), Some("all")))
          .isEmpty shouldBe true
      }

      "an optional field returns None" in new SetUp {
        validator
          .validate(ListDeductionsRawData(nino, Some("2019-04-06"), Some("2020-04-05"), None))
          .isEmpty shouldBe true
      }
    }

    "return errors" when {
      "invalid nino and source data is passed in the request" in new SetUp {
        private val result = validator.validate(ListDeductionsRawData(invalidNino, Some("2019-04-06"), Some("2020-04-05"), Some("All")))
        result shouldBe List(NinoFormatError, RuleSourceError)
      }

      "invalid dates are provided in the request" in new SetUp {
        private val result = validator.validate(ListDeductionsRawData(nino, Some("2018-04-06"), Some("2020-04-05"), Some("contractor")))
        result shouldBe List(RuleDateRangeInvalidError)
      }

      "the from and to date are not provided" in new SetUp {
        private val result = validator.validate(ListDeductionsRawData(nino, None, None, Some("customer")))
        result shouldBe List(RuleMissingFromDateError, RuleMissingToDateError)
      }

      "the from date is not in the correct format" in new SetUp {
        private val result = validator.validate(ListDeductionsRawData(nino, Some("last week"), Some("2020-04-05"), Some("customer")))
        result shouldBe List(FromDateFormatError)
      }

      "the to date is not in the correct format" in new SetUp {
        private val result = validator.validate(ListDeductionsRawData(nino, Some("2019-04-06"), Some("this week"), Some("customer")))
        result shouldBe List(ToDateFormatError)
      }

      "the from date is not the start of the tax year" in new SetUp {
        private val result = validator.validate(ListDeductionsRawData(nino, Some("2019-04-05"), Some("2020-04-05"), Some("customer")))
        result shouldBe List(RuleFromDateError)
      }

      "the to date is not the end of the tax year" in new SetUp {
        private val result = validator.validate(ListDeductionsRawData(nino, Some("2019-04-06"), Some("2020-04-04"), Some("customer")))
        result shouldBe List(RuleToDateError)
      }
    }
  }
}
