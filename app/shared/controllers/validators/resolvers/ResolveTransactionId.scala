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

package shared.controllers.validators.resolvers

import cats.data.Validated
import shared.models.domain.TransactionId
import shared.models.errors.{MtdError, TransactionIdFormatError}

object ResolveTransactionId extends ResolverSupport {

  private val transactionIdRegex = "^[0-9A-Za-z]{1,12}$".r

  val resolver: Resolver[String, TransactionId] =
    ResolveStringPattern(transactionIdRegex, TransactionIdFormatError).resolver.map(TransactionId)

  def apply(value: String): Validated[Seq[MtdError], TransactionId] = resolver(value)
}
