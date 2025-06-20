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

package v2.controllers.validators

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v2.models.request.create.CreateRequestData
import org.scalatest.TestSuite

trait MockedCreateValidatorFactory extends TestSuite with MockFactory {
  val mockedCreateValidatorFactory: CreateValidatorFactory = mock[CreateValidatorFactory]

  object MockedCreateValidatorFactory {

    def validator(): CallHandler[Validator[CreateRequestData]] =
      (mockedCreateValidatorFactory.validator(_: String, _: JsValue)).expects(*, *)

  }

  def willUseValidator(use: Validator[CreateRequestData]): CallHandler[Validator[CreateRequestData]] = {

    MockedCreateValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: CreateRequestData): Validator[CreateRequestData] =
    new Validator[CreateRequestData] {
      def validate: Validated[Seq[MtdError], CreateRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[CreateRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[CreateRequestData] =
    new Validator[CreateRequestData] {
      def validate: Validated[Seq[MtdError], CreateRequestData] = Invalid(result)
    }

}
