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

import config.AppConfig
import play.api.libs.json._
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}
import v1.models.request.create.CreateRequestData

case class CreateResponseModel(submissionId: String)

object CreateResponseModel extends HateoasLinks {
  implicit val reads: Reads[CreateResponseModel]    = Json.reads[CreateResponseModel]
  implicit val writes: OWrites[CreateResponseModel] = Json.writes[CreateResponseModel]

  implicit object CreateLinksFactory extends HateoasLinksFactory[CreateResponseModel, CreateHateoasData] {

    override def links(appConfig: AppConfig, data: CreateHateoasData): Seq[Link] = {
      import data._
      Seq(retrieveCISDeduction(appConfig, nino, requestData.body.fromDate, requestData.body.toDate, None, false))
    }

  }

}

case class CreateHateoasData(nino: String, requestData: CreateRequestData) extends HateoasData
