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

package v1.models.reponseData

import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import support.UnitSpec
import v1.models.responseData.CreateResponseModel


class CreateResponseModelSpec extends UnitSpec {


  val CisDeductionsResponseJsonObj: JsValue = Json.parse(
    """
      |{
      |"id": "S4636A77V5KB8625U"
      |}
      |""".stripMargin
  )

  val InvalidCisDeductionsResponseJsonObj: JsValue = Json.parse(
    """
      |{
      |"id": 1
      |}
      |""".stripMargin
  )

  val CisDeductionsResponseObj: CreateResponseModel = CreateResponseModel("S4636A77V5KB8625U")

  "CisDeductionsResponseModel" when {
    " write to JSON " should {
      "return the expected CisDeductionsResponseBody" in {
        Json.toJson(CisDeductionsResponseObj) shouldBe CisDeductionsResponseJsonObj
      }
    }
  }

  "Read from valid JSON" should {
    "Return the expected CisReductionsResponseBody" in {
      CisDeductionsResponseJsonObj.validate[CreateResponseModel] shouldBe JsSuccess(CisDeductionsResponseObj)
    }
  }

  "Read from invalid JSON" should {
    "return the expected error" in {
      InvalidCisDeductionsResponseJsonObj.validate[CreateResponseModel] shouldBe a[JsError]
    }
  }
}