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

package v3.mocks.services

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import shared.controllers.RequestContext
import shared.models.errors.ErrorWrapper
import shared.models.outcomes.ResponseWrapper
import v3.models.request.create.CreateRequestData
import v3.models.response.create.CreateResponseModel
import v3.services.CreateService

import scala.concurrent.{ExecutionContext, Future}

trait MockCreateService extends MockFactory {

  val mockCreateService: CreateService = mock[CreateService]

  object MockCreateService {

    def create(requestData: CreateRequestData): CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[CreateResponseModel]]]] = {
      (mockCreateService
        .createDeductions(_: CreateRequestData)(_: RequestContext, _: ExecutionContext))
        .expects(requestData, *, *)
    }

  }

}
