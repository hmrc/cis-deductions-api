package v1.models.responseData.listDeductions

import play.api.libs.json._

case class DeductionsDetails(submissionId: Option[String],
                             fromDate: String,
                             toDate: String,
                             contractorName: String,
                             employerRef: String,
                             periodData: Seq[PeriodDeductions]
                            )

object DeductionsDetails {
  implicit val reads: Reads[DeductionsDetails] = Json.reads[DeductionsDetails]
  implicit val writes: Writes[DeductionsDetails] = Json.writes[DeductionsDetails]
}