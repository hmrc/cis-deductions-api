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

package v3.models.response.retrieve

import play.api.libs.json.{JsError, JsSuccess, Json}
import shared.config.MockSharedAppConfig
import shared.hateoas.Method._
import shared.hateoas._
import shared.models.domain.TaxYear
import shared.utils.UnitSpec
import v3.fixtures.RetrieveModels.cisDeductions
import v3.fixtures.{RetrieveJson, RetrieveModels}

class RetrieveResponseModelSpec extends UnitSpec with MockSharedAppConfig {

  "RetrieveResponseModel" when {
    "processing a complete response" should {
      "produce a valid model with multiple deductions from json" in {
        Json
          .toJson(RetrieveJson.multipleDeductionsJson)
          .validate[RetrieveResponseModel[CisDeductions]] shouldBe JsSuccess(RetrieveModels.multipleDeductionsModel)
      }

      "produce a valid model with single deduction from json" in {
        Json.toJson(RetrieveJson.singleDeductionJson()).validate[RetrieveResponseModel[CisDeductions]] shouldBe JsSuccess(
          RetrieveModels.singleDeductionModel)
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
    val nino       = "mynino"
    val taxYearRaw = "2023-24"
    val source     = "all"
    val taxYear    = TaxYear.fromMtd("2023-24")

    "return the correct links" in { () =>
      MockedSharedAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()

      val hateoasData = RetrieveHateoasData(nino, taxYear, source)

      RetrieveResponseModel.CreateLinksFactory.links(mockSharedAppConfig, hateoasData) shouldBe
        Seq(
          Link(s"/my/context/$nino/current-position/$taxYearRaw/$source", GET, "self"),
          Link(s"/my/context/$nino/amendments", POST, "create-cis-deductions-for-subcontractor")
        )
    }

    "return the correct item links" in {
      MockedSharedAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()

      val hateoasData              = RetrieveHateoasData(nino, taxYear, "customer")
      val expectedDeleteHateoasUri = s"/my/context/$nino/amendments/4557ecb5-fd32-48cc-81f5-e6acd1099f3c?taxYear=2023-24"

      RetrieveResponseModel.CreateLinksFactory.itemLinks(mockSharedAppConfig, hateoasData, cisDeductions) shouldBe
        Seq(
          Link(expectedDeleteHateoasUri, DELETE, "delete-cis-deductions-for-subcontractor"),
          Link(s"/my/context/$nino/amendments/4557ecb5-fd32-48cc-81f5-e6acd1099f3c", PUT, "amend-cis-deductions-for-subcontractor")
        )
    }

  }

}
