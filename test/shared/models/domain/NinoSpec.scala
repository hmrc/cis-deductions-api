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

package shared.models.domain

import shared.utils.UnitSpec

class NinoSpec extends UnitSpec {

  "Formatting a Nino" should {
    "produce a formatted nino" in {
      val result = Nino("CS100700A").formatted
      result shouldBe "CS 10 07 00 A"
    }
  }

  "Removing a suffix" should {
    "produce a nino without a suffix" in {
      val result = Nino("AA111111A").withoutSuffix
      result shouldBe "AA111111"
    }
  }

}
