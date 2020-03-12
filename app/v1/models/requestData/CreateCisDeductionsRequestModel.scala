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

import java.util.Date

import play.api.libs.json.{Json, Reads, Writes}

case class PeriodData(
                  deductionAmount: BigDecimal,
                  deductionFromDate: String,
                  deductionToDate: String,
                  costOfMaterials: Option[BigDecimal],
                  grossAmountPaid: BigDecimal
                )

object PeriodData{
  implicit val reads: Reads[PeriodData] = Json.reads[PeriodData]
  implicit val writes: Writes[PeriodData] = Json.writes[PeriodData]
}

case class CreateCisDeductionsRequestModel(
                                       fromDate: String,
                                       toDate: String,
                                       contractorName: String,
                                       employerRef: String,
                                       periodData: Seq[PeriodData],
                                     )

object CreateCisDeductionsRequestModel {
  implicit val reads: Reads[CreateCisDeductionsRequestModel] = Json.reads[CreateCisDeductionsRequestModel]
  implicit val writes: Writes[CreateCisDeductionsRequestModel] = Json.writes[CreateCisDeductionsRequestModel]
}