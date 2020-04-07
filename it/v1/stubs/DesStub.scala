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

package v1.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}
import support.WireMockMethods
import v1.fixtures.ListJson.singleDeductionJson

object DesStub extends WireMockMethods {
  def onSuccess(method: HTTPMethod, uri: String,  nino: String, fromDate: String,
                toDate: String, source: String): StubMapping = {

    when(method = method, uri =  listdeductionsUrl(nino,fromDate,toDate,source))
      .thenReturn(status = OK, singleDeductionJson)
  }

  private val responseBody = Json.parse(
    """
      | {
      | "responseData" : "someResponse"
      | }
    """.stripMargin)

  private val deductionsResponseBody = Json.parse(
    """
      | {
      | "id" : "someResponse"
      | }
    """.stripMargin)

  private def deductionsUrl(nino: String): String =
    s"/cross-regime/deductions-placeholder/CIS/$nino"

  private def listdeductionsUrl(nino: String, fromDate : String, toDate: String, source: String): String = {
    s"cross-regime/deductions-placeholder/CIS/$nino/current-position?fromDate=$fromDate&toDate=$toDate&source=$source"
  }

    def deductionsServiceSuccess(nino: String): StubMapping = {
      when(method = POST, uri = deductionsUrl(nino))
        .thenReturn(status = OK, deductionsResponseBody)
    }

    def serviceError(nino: String, errorStatus: Int, errorBody: String): StubMapping = {
      when(method = POST, uri = deductionsUrl(nino))
        .thenReturn(status = errorStatus, errorBody)
    }
  }
