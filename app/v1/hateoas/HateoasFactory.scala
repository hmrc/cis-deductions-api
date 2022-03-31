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

package v1.hateoas

import cats.Functor
import cats.implicits._
import config.AppConfig
import javax.inject.Inject
import v1.models.hateoas._

import scala.language.higherKinds

class HateoasFactory @Inject() (appConfig: AppConfig) {

  def wrap[Payload, Data <: HateoasData](payload: Payload, data: Data)(implicit
      linksFactory: HateoasLinksFactory[Payload, Data]): HateoasWrapper[Payload] = {
    val links = linksFactory.links(appConfig, data)

    HateoasWrapper(payload, links)
  }

  def wrapList[Payload[_]: Functor, Item, Data](payload: Payload[Item], data: Data)(implicit
      linksFactory: HateoasListLinksFactory[Payload, Item, Data]): HateoasWrapper[Payload[HateoasWrapper[Item]]] = {

    val hateoasList = payload.map(item => HateoasWrapper(item, linksFactory.itemLinks(appConfig, data, item)))

    HateoasWrapper(hateoasList, linksFactory.links(appConfig, data))
  }

}

trait HateoasLinksFactory[Payload, Data] {
  def links(appConfig: AppConfig, data: Data): Seq[Link]
}

trait HateoasListLinksFactory[Payload[_], Item, Data] {
  def itemLinks(appConfig: AppConfig, data: Data, item: Item): Seq[Link]

  def links(appConfig: AppConfig, data: Data): Seq[Link]
}
