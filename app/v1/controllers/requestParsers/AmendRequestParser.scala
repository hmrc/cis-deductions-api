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

package v1.controllers.requestParsers

import v1.controllers.requestParsers.validators.AmendValidator
import v1.models.domain.{Nino, TaxYear}
import v1.models.errors.RuleIncorrectOrEmptyBodyError
import v1.models.request.amend.{AmendBody, AmendRawData, AmendRequestData}

import javax.inject.Inject

class AmendRequestParser @Inject() (val validator: AmendValidator) extends RequestParser[AmendRawData, AmendRequestData] {

  override protected def requestFor(data: AmendRawData): AmendRequestData = {
    val requestBody = data.body.as[AmendBody]

    // As the request body can have multiple period detail objects, and as the deductionToDate (which is used to validate
    // the tax year in TYS requests) must be the same for each PeriodDetails periodData object, we will take the first
    // one to represent the tax year
    val taxYear: TaxYear = requestBody.periodData.headOption match {

      case Some(periodDetails) => TaxYear.fromIso(periodDetails.deductionToDate)
      case None =>
        throw new Exception(
          s"code: ${RuleIncorrectOrEmptyBodyError.code}," +
            s" message: ${RuleIncorrectOrEmptyBodyError.message}")

      // **write tests for this in requestparser spec**
    }
    AmendRequestData(Nino(data.nino), data.id, taxYear, requestBody)
  }

}
