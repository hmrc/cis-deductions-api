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

package api.controllers.validators

import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.Valid
import cats.implicits._

/**
  * For complex additional validating that needs to take place after the initial validation and parsing of
  * the JSON payload.
  *
  * If the additional validating is fairly minor, it could just go into a method in the Validator/ValidatorFactory;
  * but if it's sizeable and is primarily about validating business rules, then it'll make sense to separate it into
  * a separate RulesValidator class.
  */
trait RulesValidator[PARSED] {

  protected val valid: Validated[Seq[MtdError], Unit] = Valid(())

  def validateBusinessRules(parsed: PARSED): Validated[Seq[MtdError], PARSED]

  protected def combine(results: Validated[Seq[MtdError], _]*): Validated[Seq[MtdError], Unit] =
    results.traverse_(identity)

  implicit class ResultOps(result: Validated[Seq[MtdError], Unit]) {
    def onSuccess(parsed: PARSED): Validated[Seq[MtdError], PARSED] = result.map(_ => parsed)
  }

}
