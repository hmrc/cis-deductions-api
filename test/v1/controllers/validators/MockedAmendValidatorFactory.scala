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

package v1.controllers.validators

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v1.models.request.amend.AmendRequestData

trait MockedAmendValidatorFactory extends MockFactory {
  val mockedAmendValidatorFactory: AmendValidatorFactory = mock[AmendValidatorFactory]

  object MockedAmendValidatorFactory {

    def validator(): CallHandler[Validator[AmendRequestData]] =
      (mockedAmendValidatorFactory.validator(_: String, _: String, _: JsValue)).expects(*, *, *)

  }

  def willUseValidator(use: Validator[AmendRequestData]): CallHandler[Validator[AmendRequestData]] = {

    MockedAmendValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: AmendRequestData): Validator[AmendRequestData] =
    new Validator[AmendRequestData] {
      def validate: Validated[Seq[MtdError], AmendRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[AmendRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[AmendRequestData] =
    new Validator[AmendRequestData] {
      def validate: Validated[Seq[MtdError], AmendRequestData] = Invalid(result)
    }

}
