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

package api.hateoas

import shared.config.MockAppConfig
import shared.hateoas.Method._
import shared.models.domain.TaxYear
import shared.utils.UnitSpec
import shared.hateoas.Link

class HateoasLinksSpec extends UnitSpec with MockAppConfig with HateoasLinks {

  private val nino         = "AA123456A"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val fromDate     = "2019-04-06"
  private val toDate       = "2020-04-05"
  private val source       = "customer"

  private val taxYear2023 = TaxYear.fromMtd("2022-23")
  private val taxYear2024 = TaxYear.fromMtd("2023-24")

  class Test {
    MockedAppConfig.apiGatewayContext.returns("individuals/deductions/cis")
  }

  "createCisDeduction" when {
    "generate the correct link with isSelf set to true" in new Test {
      val link         = createCisDeduction(mockAppConfig, nino, isSelf = true)
      val expectedHref = "/individuals/deductions/cis/AA123456A/amendments"

      link shouldBe Link(expectedHref, POST, "self")
    }

    "generate the correct link with isSelf set to false" in new Test {
      val link         = createCisDeduction(mockAppConfig, nino, isSelf = false)
      val expectedHref = "/individuals/deductions/cis/AA123456A/amendments"

      link shouldBe Link(expectedHref, POST, "create-cis-deductions-for-subcontractor")
    }
  }

  "deleteCisDeduction" when {
    "generate the correct link with isSelf set to true" in new Test {
      val link         = deleteCisDeduction(mockAppConfig, nino, submissionId, None, isSelf = true)
      val expectedHref = "/individuals/deductions/cis/AA123456A/amendments/4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

      link shouldBe Link(expectedHref, DELETE, "self")
    }

    "generate the correct link with isSelf set to false" in new Test {
      val link         = deleteCisDeduction(mockAppConfig, nino, submissionId, None, isSelf = false)
      val expectedHref = "/individuals/deductions/cis/AA123456A/amendments/4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

      link shouldBe Link(expectedHref, DELETE, "delete-cis-deductions-for-subcontractor")
    }

    "not include tax year query parameter given a non-TYS tax year" in new Test {
      val link         = deleteCisDeduction(mockAppConfig, nino, submissionId, Some(taxYear2023), isSelf = true)
      val expectedHref = "/individuals/deductions/cis/AA123456A/amendments/4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

      link shouldBe Link(expectedHref, DELETE, "self")
    }

    "include tax year query parameter given a TYS tax year" in new Test {
      val link         = deleteCisDeduction(mockAppConfig, nino, submissionId, Some(taxYear2024), isSelf = true)
      val expectedHref = "/individuals/deductions/cis/AA123456A/amendments/4557ecb5-fd32-48cc-81f5-e6acd1099f3c?taxYear=2023-24"

      link shouldBe Link(expectedHref, DELETE, "self")
    }
  }

  "amendCisDeduction" when {
    "generate the correct link with isSelf set to true" in new Test {
      val link         = amendCisDeduction(mockAppConfig, nino, submissionId, isSelf = true)
      val expectedHref = "/individuals/deductions/cis/AA123456A/amendments/4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

      link shouldBe Link(expectedHref, PUT, "self")
    }

    "generate the correct link with isSelf set to false" in new Test {
      val link         = amendCisDeduction(mockAppConfig, nino, submissionId, isSelf = false)
      val expectedHref = "/individuals/deductions/cis/AA123456A/amendments/4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

      link shouldBe Link(expectedHref, PUT, "amend-cis-deductions-for-subcontractor")
    }
  }

  "retrieveCisDeduction" when {
    "generate the correct link with isSelf set to true" in new Test {
      val link         = retrieveCisDeduction(mockAppConfig, nino, fromDate, toDate, None, isSelf = true)
      val expectedHref = "/individuals/deductions/cis/AA123456A/current-position?fromDate=2019-04-06&toDate=2020-04-05"

      link shouldBe Link(expectedHref, GET, "self")
    }

    "generate the correct link with isSelf set to false" in new Test {
      val link         = retrieveCisDeduction(mockAppConfig, nino, fromDate, toDate, None, isSelf = false)
      val expectedHref = "/individuals/deductions/cis/AA123456A/current-position?fromDate=2019-04-06&toDate=2020-04-05"

      link shouldBe Link(expectedHref, GET, "retrieve-cis-deductions-for-subcontractor")
    }

    "generate the correct link with source provided" in new Test {
      val link         = retrieveCisDeduction(mockAppConfig, nino, fromDate, toDate, Some(source), isSelf = false)
      val expectedHref = "/individuals/deductions/cis/AA123456A/current-position?fromDate=2019-04-06&toDate=2020-04-05&source=customer"

      link shouldBe Link(expectedHref, GET, "retrieve-cis-deductions-for-subcontractor")
    }
  }

}
