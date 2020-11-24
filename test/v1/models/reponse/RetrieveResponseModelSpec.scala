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

package v1.models.reponse

import mocks.MockAppConfig
import play.api.libs.json.{JsError, JsSuccess, Json}
import support.UnitSpec
import v1.fixtures._
import v1.models.hateoas.Link
import v1.models.hateoas.Method.{GET, POST}
import v1.models.response.{CisDeductions, RetrieveHateoasData, RetrieveResponseModel}

class RetrieveResponseModelSpec extends UnitSpec with MockAppConfig {

  "RetrieveResponseModel" when {
    "processing a complete response" should {
      "produce a valid model with multiple deductions from json" in {
        Json.toJson(RetrieveJson.multipleDeductionsJson)
          .validate[RetrieveResponseModel[CisDeductions]] shouldBe JsSuccess(RetrieveModels.multipleDeductionsModel)
      }
      "produce a valid model with single deduction from json" in {
        Json.toJson(RetrieveJson.singleDeductionJson).validate[RetrieveResponseModel[CisDeductions]] shouldBe JsSuccess(RetrieveModels.singleDeductionModel)
      }
      "produce a valid model with single deduction(contractor submission only) from json" in {
        Json.toJson(RetrieveJson.singleDeductionContractorJson).validate[RetrieveResponseModel[CisDeductions]] shouldBe
          JsSuccess(RetrieveModels.singleDeductionModelContractor)
      }
    }
    "processing bad json" should {
      "produce an error" in {
        Json.parse(RetrieveJson.errorJson).validate[RetrieveResponseModel[CisDeductions]] shouldBe a[JsError]
      }
    }
    "producing json from a valid model" should {
      "produce valid json" in {
        Json.toJson(RetrieveModels.multipleDeductionsModel) shouldBe Json.toJson(RetrieveJson.multipleDeductionsJson)
      }
    }
  }

  "LinksFactory" should {
    "return the correct links" in {
      val nino = "mynino"
      val fromDate = "fromDate"
      val toDate = "toDate"

      MockedAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes
      RetrieveResponseModel.CreateLinksFactory.links(mockAppConfig, RetrieveHateoasData(nino, fromDate, toDate, None, RetrieveModels.multipleDeductionsModel)) shouldBe
        Seq(
          Link(s"/my/context/$nino/current-position?fromDate=$fromDate&toDate=$toDate", GET, "self"),
          Link(s"/my/context/$nino/amendments", POST, "create-cis-deductions-for-subcontractor"),
        )
    }

  }
}