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

package v1.models.responseData.listDeductions

import cats.Functor
import config.AppConfig
import play.api.libs.json._
import v1.hateoas.{HateoasLinks, HateoasListLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class ListResponseModel[I](cisDeductions: Seq[I])

object ListResponseModel extends HateoasLinks {

  implicit def reads[I: Reads]: Reads[ListResponseModel[I]] = implicitly(Json.reads[ListResponseModel[I]])

  implicit def writes[I: Writes]: OWrites[ListResponseModel[I]] = Json.writes[ListResponseModel[I]]

  implicit object CreateLinksFactory extends HateoasListLinksFactory[ListResponseModel, DeductionsDetails, ListResponseHateoasData] {

    override def itemLinks(appConfig: AppConfig, data: ListResponseHateoasData, item: DeductionsDetails): Seq[Link] = {
      item.submissionId match {
          case None => Seq()
          case _ => Seq(deleteCISDeduction(appConfig, data.nino, item.submissionId.getOrElse(""), isSelf = false),
            amendCISDeduction(appConfig, data.nino, item.submissionId.getOrElse(""), isSelf = false))
        }
    }

    override def links(appConfig: AppConfig, data: ListResponseHateoasData): Seq[Link] = {
      Seq(listCISDeduction(appConfig, data.nino, data.fromDate, data.toDate, data.source.getOrElse("all"), isSelf = true),
        createCISDeduction(appConfig, data.nino, isSelf = false))
    }
  }

  implicit object ResponseFunctor extends Functor[ListResponseModel] {
    override def map[A, B](fa: ListResponseModel[A])(f: A => B): ListResponseModel[B] =
      ListResponseModel(fa.cisDeductions.map(f))
  }
}

case class ListResponseHateoasData(nino: String, fromDate: String, toDate: String, source: Option[String],
                                   listResponse: ListResponseModel[DeductionsDetails]) extends HateoasData
