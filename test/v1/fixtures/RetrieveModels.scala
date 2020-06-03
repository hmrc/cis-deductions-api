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

package v1.fixtures

import v1.models.responseData.{DeductionsDetails, RetrieveResponseModel, PeriodDeductions}

object RetrieveModels {

  val multipleDeductionsModel = RetrieveResponseModel(
    Seq(
      DeductionsDetails(
        Some("54759eb3c090d83494e2d804"),
        "2019-04-06",
        "2020-04-05",
        "Bovis",
        "BV40092",
        Seq(
          PeriodDeductions(355.11, "2019-04-06", "2019-05-05", Some(35.11), 1457.11, "2019-04-03", "contractor"),
          PeriodDeductions(355.11, "2019-05-06", "2019-06-05", Some(35.11), 1457.11, "2019-05-03", "contractor"),
          PeriodDeductions(355.11, "2019-06-06", "2019-07-05", Some(35.11), 1457.11, "2020-01-14", "customer"),
          PeriodDeductions(355.11, "2019-07-06", "2019-08-05", Some(35.11), 1457.11, "2020-01-14", "customer")
        )
      ),
      DeductionsDetails(
        None,
        "2019-04-06",
        "2020-04-05",
        "Taylor Wimpy",
        "TW44355",
        Seq(
          PeriodDeductions(60.11, "2019-07-06", "2019-08-05", Some(35.11), 1457.11, "2019-08-05", "contractor"),
          PeriodDeductions(60.11, "2019-09-06", "2019-10-05", Some(35.11), 1457.11, "2019-08-05", "contractor")
        )
      )
    )
  )

  val singleDeductionModel = RetrieveResponseModel(
    Seq(
      DeductionsDetails(
        Some("54759eb3c090d83494e2d804"),
        "2019-04-06",
        "2020-04-05",
        "Bovis",
        "BV40092",
        Seq(
          PeriodDeductions(355.00, "2019-06-06", "2019-07-05", Some(35.00), 1457.00, "2020-01-14", "customer"),
          PeriodDeductions(355.00, "2019-07-06", "2019-08-05", Some(35.00), 1457.00, "2020-01-14", "customer")
        )
      )
    )
  )

  val retrieveCisDeductionsModel =
    RetrieveResponseModel(
      Seq(DeductionsDetails(
        submissionId = Some("12345678"),
        fromDate = "2019-04-06",
        toDate = "2020-04-05",
        contractorName = "Bovis",
        employerRef = "BV40092",
        Seq(
          PeriodDeductions(
            deductionAmount = 355.00,
            deductionFromDate = "2019-06-06",
            deductionToDate = "2019-07-05",
            costOfMaterials = Some(35.00),
            grossAmountPaid = 1457.00,
            submissionDate = " 2019-04-06",
            submittedBy = "2019-04-06"
          )
        )
      )
      )
    )

}
