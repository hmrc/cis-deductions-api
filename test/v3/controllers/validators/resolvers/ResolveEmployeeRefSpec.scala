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

package v3.controllers.validators.resolvers

import cats.data.Validated.{Invalid, Valid}
import models.errors.EmployerRefFormatError
import shared.utils.UnitSpec
import v3.models.request.create.EmployeeRef

class ResolveEmployeeRefSpec extends UnitSpec {

  "ResolveEmployeeRef" should {
    "return no errors" when {
      "passed a valid employee ref" in {
        val validEmployeeRef = "123/123456798"
        val result           = ResolveEmployeeRef(validEmployeeRef)
        result shouldBe Valid(EmployeeRef(validEmployeeRef))
      }
    }

    "return an error" when {
      "passed an invalid employee ref" in {
        val invalidEmployeeRef = "12/123456789"
        val result             = ResolveEmployeeRef(invalidEmployeeRef)
        result shouldBe Invalid(List(EmployerRefFormatError))
      }
    }
  }

}
