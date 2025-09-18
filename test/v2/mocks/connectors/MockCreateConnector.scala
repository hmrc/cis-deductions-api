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

package v2.mocks.connectors

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import shared.connectors.DownstreamOutcome
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.CreateConnector
import v2.models.request.create.CreateRequestData
import v2.models.response.create.CreateResponseModel
import org.scalatest.TestSuite

import scala.concurrent.{ExecutionContext, Future}

trait MockCreateConnector extends TestSuite with MockFactory {

  val mockCreateConnector: CreateConnector = mock[CreateConnector]

  object MockCreateCisDeductionsConnector {

    def createCisDeduction(requestData: CreateRequestData): CallHandler[Future[DownstreamOutcome[CreateResponseModel]]] = {
      (mockCreateConnector
        .create(_: CreateRequestData)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(requestData, *, *, *)
    }

  }

}
