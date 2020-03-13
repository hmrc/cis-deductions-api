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
import v1.models.responseData.CreateCisDeductionsResponseModel


class CreateCisDeductionsResponseModelSpec extends UnitSpec {


  val CisDeductionsJsonObj: JsValue = Json.parse(
    """
      |{
      |"id": "S4636A77V5KB8625U"
      |}
      |""".stripMargin
  )

  val InvalidCisDeductionsJsonObj: JsValue = Json.parse(
    """
      |{
      |"id": 1
      |}
      |""".stripMargin
  )

  val CisDeductionsObj: CreateCisDeductionsResponseModel = CreateCisDeductionsResponseModel("S4636A77V5KB8625U")

  "CisDeductionsResponseModel" when {
    " written to JSON " should {
      "return the expected CisDeductionsResponseBody" in {
        Json.toJson(CisDeductionsObj) shouldBe CisDeductionsJsonObj
      }
    }
  }

  "Read from valid JSON" should {
    "Return the expected CisReductionsResponseBody" in {
      CisDeductionsJsonObj.validate[CreateCisDeductionsResponseModel] shouldBe JsSuccess(CisDeductionsObj)
    }
  }

  "Read from invalid JSON" should {
    "return the expected error" in {
      InvalidCisDeductionsJsonObj.validate[CreateCisDeductionsResponseModel] shouldBe a[JsError]
    }
  }




}
