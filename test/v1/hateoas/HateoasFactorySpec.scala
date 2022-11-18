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
import config.AppConfig
import mocks.MockAppConfig
import play.api.libs.json.{Json, OWrites}
import support.UnitSpec
import v1.models.hateoas.Method.GET
import v1.models.hateoas.{HateoasData, HateoasWrapper, Link}

class HateoasFactorySpec extends UnitSpec with MockAppConfig {

  val hateoasFactory = new HateoasFactory(mockAppConfig)

  case class Response(foo: String)
  case class RetrieveResponse[A](items: Seq[A])

  case class Data1(id: String) extends HateoasData
  case class Data2(id: String) extends HateoasData

  val response = Response("X")

  object Response {
    implicit val writes: OWrites[Response] = Json.writes[Response]
  }

  object RetrieveResponse {
    implicit val writes: OWrites[RetrieveResponse[HateoasWrapper[Response]]] = Json.writes[RetrieveResponse[HateoasWrapper[Response]]]
  }

  class Test {
    MockedAppConfig.apiGatewayContext.returns("context").anyNumberOfTimes()

  }

  "wrap" should {

    implicit object LinksFactory1 extends HateoasLinksFactory[Response, Data1] {
      override def links(appConfig: AppConfig, data: Data1): Seq[Link] = Seq(Link(s"${appConfig.apiGatewayContext}/${data.id}", GET, "rel1"))
    }

    implicit object LinksFactory2 extends HateoasLinksFactory[Response, Data2] {
      override def links(appConfig: AppConfig, data: Data2): Seq[Link] = Seq(Link(s"${appConfig.apiGatewayContext}/${data.id}", GET, "rel2"))
    }

    "use the response specific links" in new Test {

      val wrapper = HateoasWrapper(response, Seq(Link("context/id", GET, "rel1")))
      hateoasFactory.wrap(response, Data1("id")) shouldBe wrapper

      Json.toJson(wrapper) shouldBe
        Json.parse("""
          |{
          |"foo": "X",
          |"links" : [
          |  {
          |    "href": "context/id",
          |    "method": "GET",
          |    "rel": "rel1"
          |  }
          | ]
          |}
          |""".stripMargin)

    }

    "use the endpoint HateoasData specific links" in new Test {
      val wrapper = HateoasWrapper(response, Seq(Link("context/id", GET, "rel2")))
      hateoasFactory.wrap(response, Data2("id")) shouldBe wrapper

      Json.toJson(wrapper) shouldBe
        Json.parse("""
                     |{
                     |"foo": "X",
                     |"links" : [
                     |  {
                     |    "href": "context/id",
                     |    "method": "GET",
                     |    "rel": "rel2"
                     |  }
                     | ]
                     |}
                     |""".stripMargin)
    }
  }

  "wrapList" should {

    implicit object ListResponseFunctor extends Functor[RetrieveResponse] {
      override def map[A, B](fa: RetrieveResponse[A])(f: A => B): RetrieveResponse[B] = RetrieveResponse(fa.items.map(f))
    }

    implicit object LinksFactory extends HateoasListLinksFactory[RetrieveResponse, Response, Data1] {
      override def itemLinks(appConfig: AppConfig, data: Data1, item: Response): Seq[Link] =
        Seq(Link(s"${appConfig.apiGatewayContext}/${data.id}/${item.foo}", GET, "item"))

      override def links(appConfig: AppConfig, data: Data1): Seq[Link] = Seq(Link(s"${appConfig.apiGatewayContext}/${data.id}", GET, "rel"))
    }

    "work" in new Test {

      val wrapper =
        HateoasWrapper(RetrieveResponse(Seq(HateoasWrapper(response, Seq(Link("context/id/X", GET, "item"))))), Seq(Link("context/id", GET, "rel")))

      hateoasFactory.wrapList(RetrieveResponse(Seq(response)), Data1("id")) shouldBe wrapper

      Json.toJson(wrapper) shouldBe
        Json.parse("""
                     |{
                     |	"items": [{
                     |		"foo": "X",
                     |		"links": [{
                     |			"href": "context/id/X",
                     |			"method": "GET",
                     |			"rel": "item"
                     |		}]
                     |	}],
                     |	"links": [{
                     |		"href": "context/id",
                     |		"method": "GET",
                     |		"rel": "rel"
                     |	}]
                     |}
                     |""".stripMargin)
    }
  }

}
