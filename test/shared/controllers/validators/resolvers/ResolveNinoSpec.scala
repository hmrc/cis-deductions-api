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

package shared.controllers.validators.resolvers

import cats.data.Validated.{Invalid, Valid}
import shared.UnitSpec
import shared.models.domain.Nino
import shared.models.errors.NinoFormatError

class ResolveNinoSpec extends UnitSpec {

  "ResolveNino" should {
    "return the parsed Nino" when {
      "given a valid nino string" in {
        val validNino = "AA123456A"
        val result    = ResolveNino(validNino)
        result shouldBe Valid(Nino(validNino))
      }
    }

    "return an error" when {
      "given an invalid NINO" in {
        val invalidNino = "AA123456ABCBBCBCBC"
        val result      = ResolveNino(invalidNino)
        result shouldBe Invalid(List(NinoFormatError))
      }
    }
  }

}
