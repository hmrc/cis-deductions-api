/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.models.response.create

import play.api.libs.json.{JsError, JsSuccess, Json}
import shared.config.MockSharedAppConfig
import shared.utils.UnitSpec
import v3.fixtures.CreateRequestFixtures._

class CreateResponseModelSpec extends UnitSpec with MockSharedAppConfig {

  "CisDeductionsResponseModel" when {
    " write to JSON " should {
      "return the expected CisDeductionsResponseBody" in {
        Json.toJson(responseObj) shouldBe responseJson
      }
    }
  }

  "Read from valid JSON" should {
    "Return the expected CisReductionsResponseBody" in {
      responseJson.validate[CreateResponseModel] shouldBe JsSuccess(responseObj)
    }
  }

  "Read from invalid JSON" should {
    "return the expected error when invalid data type is used" in {
      invalidResponseJson.validate[CreateResponseModel] shouldBe a[JsError]
    }

    "return the expected error when submission id field is missing" in {
      missingMandatoryResponseJson.validate[CreateResponseModel] shouldBe a[JsError]
    }
  }

}
