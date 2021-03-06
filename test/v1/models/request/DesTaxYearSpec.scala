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

package v1.models.request

import support.UnitSpec

class DesTaxYearSpec extends UnitSpec {

  val mtdValue = "2018-19"
  val desValue = "2019"

  "DesTaxYear" when {
    "toString is called" should {
      "return the value instead of a String representation of the case class" in {
        DesTaxYear(desValue).toString shouldBe desValue
      }
    }

    "fromMtd is called" should {
      "return the DES representation of the tax year" in {
        DesTaxYear.fromMtd(mtdValue) shouldBe DesTaxYear(desValue)
      }
    }
  }


}
