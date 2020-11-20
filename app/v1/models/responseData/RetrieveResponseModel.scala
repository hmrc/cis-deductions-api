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

package v1.models.responseData

import cats.Functor
import config.AppConfig
import play.api.libs.json._
import v1.hateoas.{HateoasLinks, HateoasListLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class RetrieveResponseModel[I](totalDeductionAmount: Option[BigDecimal],
                                    totalCostOfMaterials: Option[BigDecimal],
                                    totalGrossAmountPaid: Option[BigDecimal],
                                    cisDeductions: Seq[I])

object RetrieveResponseModel extends HateoasLinks {

  implicit def reads[I: Reads]: Reads[RetrieveResponseModel[I]] = implicitly(Json.reads[RetrieveResponseModel[I]])

  implicit def writes[I: Writes]: OWrites[RetrieveResponseModel[I]] = Json.writes[RetrieveResponseModel[I]]

  implicit object CreateLinksFactory extends HateoasListLinksFactory[RetrieveResponseModel, CisDeductions, RetrieveHateoasData] {

    override def itemLinks(appConfig: AppConfig, data: RetrieveHateoasData, item: CisDeductions): Seq[Link] = {
      val filteredPeriodData = item.periodData.filter(_.submissionId.isDefined)
      val submissionIdOption = filteredPeriodData.headOption.map(_.submissionId.get)

      filteredPeriodData match {
        case Nil => Seq()
        case _ => {
          Seq(deleteCISDeduction(appConfig, data.nino, submissionIdOption.getOrElse(""), isSelf = false),
            amendCISDeduction(appConfig, data.nino, submissionIdOption.getOrElse(""), isSelf = false))
        }
      }
    }
    override def links(appConfig: AppConfig, data: RetrieveHateoasData): Seq[Link] = {
      Seq(retrieveCISDeduction(appConfig, data.nino, data.fromDate, data.toDate, data.source, isSelf = true),
        createCISDeduction(appConfig, data.nino, isSelf = false))
    }
  }
  implicit object ResponseFunctor extends Functor[RetrieveResponseModel] {
    override def map[A, B](fa: RetrieveResponseModel[A])(f: A => B): RetrieveResponseModel[B] =
      RetrieveResponseModel(fa.totalDeductionAmount, fa.totalCostOfMaterials, fa.totalGrossAmountPaid, fa.cisDeductions.map(f))
  }
}

case class RetrieveHateoasData(nino: String, fromDate: String, toDate: String, source: Option[String],
                               retrieveResponse: RetrieveResponseModel[CisDeductions]) extends HateoasData
