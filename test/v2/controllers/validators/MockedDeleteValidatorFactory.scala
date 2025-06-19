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
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v2.models.request.delete.DeleteRequestData
import org.scalatest.TestSuite

trait MockedDeleteValidatorFactory extends TestSuite with MockFactory {
  val mockedDeleteValidatorFactory: DeleteValidatorFactory = mock[DeleteValidatorFactory]

  object MockedDeleteValidatorFactory {

    def validator(): CallHandler[Validator[DeleteRequestData]] =
      (mockedDeleteValidatorFactory.validator(_: String, _: String, _: Option[String])).expects(*, *, *)

  }

  def willUseValidator(use: Validator[DeleteRequestData]): CallHandler[Validator[DeleteRequestData]] = {

    MockedDeleteValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: DeleteRequestData): Validator[DeleteRequestData] =
    new Validator[DeleteRequestData] {
      def validate: Validated[Seq[MtdError], DeleteRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[DeleteRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[DeleteRequestData] =
    new Validator[DeleteRequestData] {
      def validate: Validated[Seq[MtdError], DeleteRequestData] = Invalid(result)
    }

}
