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

import play.api.libs.json.{JsError, JsSuccess, Json}
import v1.fixtures.CreateRequestFixtures.{ invalidResponseJson, missingMandatoryResponseJson, responseJson}
import v1.fixtures.AmendRequestFixtures.amendResponseObj
import support.UnitSpec
import v1.models.responseData.AmendResponse



class AmendResponseSpec extends UnitSpec {
  "CisDeductionsResponseModel" when {
    " write to JSON " should {
      "return the expected CisDeductionsResponseBody" in {
        Json.toJson(amendResponseObj) shouldBe responseJson
      }
    }
  }

  "Read from valid JSON" should {
    "Return the expected CisReductionsResponseBody" in {
      responseJson.validate[AmendResponse] shouldBe JsSuccess(amendResponseObj)
    }
  }

  "Read from invalid JSON" should {
    "return the expected error when invalid data type is used" in {
      invalidResponseJson.validate[AmendResponse] shouldBe a[JsError]
    }

    "return the expected error when id field is missing" in {
      missingMandatoryResponseJson.validate[AmendResponse] shouldBe a[JsError]
    }
  }
}
