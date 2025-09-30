/*
 * Copyright 2025 HM Revenue & Customs
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

package shared.utils

import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import shared.utils.NestedJsonReads.*

class NestedJsonReadsSpec extends UnitSpec {

  val firstOutput: JsValue = Json.parse("""{
      | "a" : {
      |   "b" : {
      |     "c" : "string"
      |   },
      |   "i" : {
      |     "e" : "example"
      |   }
      |  }
      |}""".stripMargin)

  val secondOutput: JsValue = Json.parse("""{
      | "a" : {
      |   "b" : {
      |     "c" : "string"
      |   },
      |   "c" : {
      |     "e" : "example"
      |   }
      |  }
      |}""".stripMargin)

  val thirdOutput: JsValue = Json.parse("""{
      | "a" : {
      |   "b" : {
      |     "c" : "string"
      |   },
      |   "c" : {
      |     "e" : 2
      |   }
      |  }
      |}""".stripMargin)

  val fourthOutput: JsValue = Json.parse("""{
      | "a" : {
      |   "b" : {
      |     "c" : "string"
      |   },
      |   "c" : {
      |   }
      |  }
      |}""".stripMargin)

  val jsonWithNull: JsValue = Json.parse("""{
      | "a" : {
      |   "b" : {
      |     "c" : "string"
      |   },
      |   "c" : {
      |     "e" : null
      |   }
      |  }
      |}""".stripMargin)

  "Valid Json" should {

    "return JsSuccess" in {
      firstOutput.validate[Test] shouldBe a[JsSuccess[?]]
    }
  }

  "An empty JsPath" should {
    "return a JsError with error.path.empty" in {
      val empty                            = JsPath
      val result: JsResult[Option[String]] = empty.readNestedNullable[String].reads(Json.obj("a" -> "b"))
      result shouldBe a[JsError]
      result.asInstanceOf[JsError].errors.head._2.exists(_.message == "error.path.empty") shouldBe true
    }
  }

  "A missing path" should {
    "return a None" in {
      firstOutput.as[Test] shouldBe Test("string", None)
    }
  }

  "With no missing sections" should {
    "return a full test as the second parameter" in {
      secondOutput.as[Test] shouldBe Test("string", Some("example"))
    }
  }

  "With no missing sections and a null value" should {
    "return a full test as the second parameter" in {
      jsonWithNull.as[Test] shouldBe Test("string", None)
    }
  }

  "Path with an invalid data type" should {
    "return a None " in {
      thirdOutput.validate[Test] shouldBe a[JsError]
    }
  }

  "Empty path" should {
    "return a None " in {
      fourthOutput.validate[Test] shouldBe a[JsSuccess[?]]
    }
  }

  case class Test(param: String, param3: Option[String])

  object Test {

    given Reads[Test] = (
      (JsPath \ "a" \ "b" \ "c").read[String] and
        (__ \ "a" \ "c" \ "e").readNestedNullable[String]
    )(Test.apply)

  }

}
