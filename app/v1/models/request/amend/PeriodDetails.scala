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

package v1.models.request.amend

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class PeriodDetails(deductionAmount: BigDecimal,
                         deductionFromDate: String,
                         deductionToDate: String,
                         costOfMaterials: Option[BigDecimal],
                         grossAmountPaid: Option[BigDecimal])

object PeriodDetails {

  implicit val reads: Reads[PeriodDetails] = (
    (JsPath \ "deductionAmount").read[BigDecimal] and
      (JsPath \ "deductionFromDate").read[String] and
      (JsPath \ "deductionToDate").read[String] and
      (JsPath \ "costOfMaterials").readNullable[BigDecimal] and
      (JsPath \ "grossAmountPaid").readNullable[BigDecimal]
  )(PeriodDetails.apply _)

  implicit val writes: OWrites[PeriodDetails] = Json.writes[PeriodDetails]

}
