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

package v2.models.errors

import play.api.http.Status.BAD_REQUEST
import shared.models.errors.MtdError

object CisDeductionsApiCommonErrors {

  // MtdError types that are common to CIS DEDUCTIONS API.

  object DeductionFromDateFormatError extends MtdError("FORMAT_DEDUCTIONS_FROM_DATE", "The provided deductions From date is invalid", BAD_REQUEST)
  object DeductionToDateFormatError   extends MtdError("FORMAT_DEDUCTIONS_TO_DATE", "The provided deductions To date is invalid", BAD_REQUEST)

}
