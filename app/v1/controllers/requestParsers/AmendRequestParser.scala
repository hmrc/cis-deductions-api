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

import api.controllers.requestParsers.RequestParser
import api.models.domain.{Nino, TaxYear}
import v1.controllers.requestParsers.validators.AmendValidator
import v1.models.request.amend.{AmendBody, AmendRawData, AmendRequestData}

import javax.inject.Inject

class AmendRequestParser @Inject() (val validator: AmendValidator) extends RequestParser[AmendRawData, AmendRequestData] {

  override protected def requestFor(data: AmendRawData): AmendRequestData = {
    val requestBody = data.body.as[AmendBody]

    /* The `deductionToDate` fields in the PeriodDetails periodData objects are validated to ensure all dates point to
    the same tax year */
    val taxYear: TaxYear = requestBody.periodData.headOption match {

      case Some(periodDetails) => TaxYear.fromIso(periodDetails.deductionToDate)
      case None                => throw new Exception("Unable to locate `deductionToDate` in request body")

    }
    AmendRequestData(Nino(data.nino), data.submissionId, taxYear, requestBody)
  }

}
