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

package v1.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations.{
  NinoValidation,
  SubmissionIdValidation,
  TaxYearTysParameterValidation,
  TaxYearValidation
}
import api.models.errors.MtdError
import v1.models.request.delete.DeleteRawData

class DeleteValidator extends Validator[DeleteRawData] {

  private val validations = List(parameterFormatValidation)

  private def parameterFormatValidation: DeleteRawData => List[List[MtdError]] = (data: DeleteRawData) => {
    List(
      NinoValidation.validate(data.nino),
      SubmissionIdValidation.validate(data.submissionId),
      TaxYearValidation.validate(data.taxYear),
      TaxYearTysParameterValidation.validate(data.taxYear)
    )
  }

  override def validate(data: DeleteRawData): List[MtdError] = {
    run(validations, data).distinct
  }

}
