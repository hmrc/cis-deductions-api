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

package v1.models.response.create

import api.hateoas.HateoasLinks
import shared.hateoas._
import play.api.libs.json._
import shared.config.AppConfig
import shared.hateoas.{HateoasData, HateoasLinksFactory}
import v1.models.request.create.CreateRequestData

case class CreateResponseModel(submissionId: String)

object CreateResponseModel extends HateoasLinks {
  implicit val reads: Reads[CreateResponseModel]    = Json.reads[CreateResponseModel]
  implicit val writes: OWrites[CreateResponseModel] = Json.writes[CreateResponseModel]

  implicit object CreateLinksFactory extends HateoasLinksFactory[CreateResponseModel, CreateHateoasData] {

    override def links(appConfig: AppConfig, data: CreateHateoasData): Seq[Link] = {
      import data._
      Seq(retrieveCisDeduction(appConfig, nino, requestData.body.fromDate, requestData.body.toDate, None, false))
    }

  }

}

case class CreateHateoasData(nino: String, requestData: CreateRequestData) extends HateoasData
