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

import play.api.libs.json.{JsError, JsSuccess, Json}
import support.UnitSpec
import v1.models.request.AmendRequest
import v1.fixtures.RequestFixtures._
import v1.fixtures.AmendRequestFixtures.{amendMissingOptionalRequestObj,amendRequestObj}



class AmendRequestSpec extends UnitSpec {

  "read from valid JSON" should {
    "return the expected CisDeductionsRequestBody" in {
      requestJson.validate[AmendRequest] shouldBe JsSuccess(amendRequestObj)
    }
    "return the expected CisDeductionRequestBody when optional field is omitted" in {
      missingOptionalRequestJson.validate[AmendRequest] shouldBe JsSuccess(amendMissingOptionalRequestObj)
    }
  }

  "read from invalid JSON" should {
    "return the expected error when field contains incorrect data type" in {
      invalidRequestJson.validate[AmendRequest] shouldBe a[JsError]
    }

    "return the expected error when mandatory field is omitted" in {
      missingMandatoryFieldRequestJson.validate[AmendRequest] shouldBe a[JsError]
    }
  }

  " written to JSON " should {
    "return the expected CisDeductionsRequestBody" in {
      Json.toJson(amendRequestObj) shouldBe requestJson
    }
  }
}
