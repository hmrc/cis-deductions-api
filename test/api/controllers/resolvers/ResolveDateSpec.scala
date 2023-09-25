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

package api.controllers.resolvers

import api.models.errors.{StartDateFormatError, InternalError}
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

class ResolveDateSpec extends UnitSpec {

  private val validDate    = "2020-01-01"
  private val invalidDate  = "not-a-date"
  private val invalidError = StartDateFormatError

  "ResolveDateRange" should {
    "return no errors" when {
      "passed a valid date" in {
        val result = ResolveDate(validDate)
        result shouldBe Valid(validDate)
      }
    }

    "return an error" when {
      "passed an invalid date and error" in {
        val result = ResolveDate(invalidDate, invalidError)
        result shouldBe Invalid(List(StartDateFormatError))
      }

      "passed an invalid date" in {
        val result = ResolveDate(invalidDate)
        result shouldBe Invalid(List(InternalError))
      }

    }
  }

}
