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
import v1.models.errors.{NinoFormatError, RuleDateRangeInvalidError, RuleSourceError}
import v1.models.request.ListDeductionsRawData

class ListDeductionsValidatorSpec extends UnitSpec {

  val nino = "AA123456A"
  val invalidNino = "GHFG197854"
  val validListRawData = ListDeductionsRawData(nino, "2019-04-06", "2020-04-05", Some("all"))
  val validOptianListRawData = ListDeductionsRawData(nino, "2019-04-06", "2020-04-05", None)

  val invalidListRawData = ListDeductionsRawData(invalidNino, "2019-04-06", "2020-04-05", Some("All"))
  val invalidDateListData = ListDeductionsRawData(nino, "2018-04-06", "2020-04-05", Some("contractor"))

  class SetUp {
    val validator = new ListDeductionsValidator
  }

  "running validation" should {
    "return no errors" when {
      "all query parameters are passed in the request" in new SetUp {
        validator
          .validate(validListRawData)
          .isEmpty shouldBe true
      }

      "an optional field returns None" in new SetUp {
        validator
          .validate(validOptianListRawData)
          .isEmpty shouldBe true
      }
    }

    "return errors" when {
      "invalid nino and source data is passed in the request" in new SetUp{
        private val result = validator.validate(invalidListRawData)
        result.length shouldBe 2
        result shouldBe List(NinoFormatError,RuleSourceError)
      }

      "invalid dates are provided in the request" in new SetUp{
        private val result = validator.validate(invalidDateListData)
        result.length shouldBe 1
        result shouldBe List(RuleDateRangeInvalidError)
      }
    }
  }

}
