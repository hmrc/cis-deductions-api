/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.response.create

import mocks.MockAppConfig
import play.api.libs.json.{JsError, JsSuccess, Json}
import support.UnitSpec
import v1.fixtures.CreateRequestFixtures._
import v1.models.domain.Nino
import v1.models.hateoas.Link
import v1.models.hateoas.Method.GET
import v1.models.request.amend.PeriodDetails
import v1.models.request.create.{CreateBody, CreateRequestData}

class CreateResponseModelSpec extends UnitSpec with MockAppConfig {

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

  "LinksFactory" should {
    "return the correct links" in {
      val nino           = "AA999999A"
      val fromDate       = "fromDate"
      val toDate         = "toDate"
      val contractorName = "name"
      val employerRef    = "reference"
      val periodData     = Seq(PeriodDetails(11.12, fromDate, toDate, None, None))
      val request        = CreateRequestData(Nino(nino), CreateBody(fromDate, toDate, contractorName, employerRef, periodData))

      MockedAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes
      CreateResponseModel.CreateLinksFactory
        .links(mockAppConfig, CreateHateoasData(nino, request)) shouldBe
        Seq(
          Link(s"/my/context/$nino/current-position?fromDate=$fromDate&toDate=$toDate", GET, "retrieve-cis-deductions-for-subcontractor")
        )
    }

  }

}
