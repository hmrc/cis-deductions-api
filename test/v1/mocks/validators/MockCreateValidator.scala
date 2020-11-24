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

package v1.mocks.validators

import org.scalamock.handlers.CallHandler1
import org.scalamock.scalatest.MockFactory
import v1.controllers.requestParsers.validators.CreateValidator
import v1.models.errors.MtdError
import v1.models.request.create.CreateRawData

class MockCreateValidator extends MockFactory {

  val mockValidator: CreateValidator =  mock[CreateValidator]

  object MockValidator {

    def validate(data: CreateRawData): CallHandler1[CreateRawData, List[MtdError]] = {
      (mockValidator
        .validate(_:CreateRawData))
        .expects(data)
    }
  }
}
