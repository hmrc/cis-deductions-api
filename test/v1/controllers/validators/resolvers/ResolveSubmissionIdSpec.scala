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

package v1.controllers.validators.resolvers

import cats.data.Validated.{Invalid, Valid}
import models.errors.SubmissionIdFormatError
import shared.utils.UnitSpec
import v1.controllers.validators.resolvers
import v1.models.domain.SubmissionId

class ResolveSubmissionIdSpec extends UnitSpec {

  "ResolveSubmissionId" should {
    "return no errors" when {
      "passed a valid submission ID" in {
        val id     = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
        val result = ResolveSubmissionId(id)
        result shouldBe Valid(SubmissionId(id))
      }
    }

    "return an error" when {
      "passed an invalid submission ID" in {
        val id     = "12345678-1234-123-123-123456789012"
        val result = resolvers.ResolveSubmissionId(id)
        result shouldBe Invalid(List(SubmissionIdFormatError))
      }
    }
  }

}
