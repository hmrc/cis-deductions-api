package v1.models.responseData.listDeductions

import play.api.libs.json._

case class PeriodDeductions(deductionAmount: BigDecimal,
                            deductionFromDate: String,
                            deductionToDate: String,
                            costOfMaterials: Option[BigDecimal],
                            grossAmountPaid: BigDecimal,
                            submissionDate: String,
                            submittedBy: String
                            )

object PeriodDeductions {
  implicit val reads: Reads[PeriodDeductions] = Json.reads[PeriodDeductions]
  implicit val writes: Writes[PeriodDeductions] = Json.writes[PeriodDeductions]
}