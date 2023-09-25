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

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v1.models.request.retrieve.RetrieveRequestData

trait MockedRetrieveValidatorFactory extends MockFactory {
  val mockedRetrieveValidatorFactory: RetrieveValidatorFactory = mock[RetrieveValidatorFactory]

  object MockedRetrieveValidatorFactory {

    def validator(): CallHandler[Validator[RetrieveRequestData]] =
      (mockedRetrieveValidatorFactory.validator(_: String, _: Option[String], _: Option[String], _: Option[String])).expects(*, *, *, *)

  }

  def willUseValidator(use: Validator[RetrieveRequestData]): CallHandler[Validator[RetrieveRequestData]] = {

    MockedRetrieveValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: RetrieveRequestData): Validator[RetrieveRequestData] =
    new Validator[RetrieveRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrieveRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrieveRequestData] =
    new Validator[RetrieveRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveRequestData] = Invalid(result)
    }

}
