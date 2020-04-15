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

package v1.models.requestData

import org.scalatest.Inside
import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v1.models.errors.NinoFormatError
import v1.models.request.{DeleteRequest}

class DeleteRequestSpec extends Inside with UnitSpec {

  val nino = "AA123456A"
  val id = "S4636A77V5KB8625U"


  val deleteRequest = DeleteRequest(Nino(nino), id)

  "DeleteRequest" should {
      "should equal the correct representation of the case class given the parameters" in {
        inside(deleteRequest) { case DeleteRequest(nino, id) =>
          nino shouldBe (Nino("AA123456A"))
          id shouldBe ("S4636A77V5KB8625U")
        }
      }
    }
}
