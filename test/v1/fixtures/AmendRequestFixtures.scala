/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.fixtures

import v1.models.request.{AmendRequest, PeriodDetails}
import v1.models.responseData.AmendResponse

object AmendRequestFixtures {

  val amendRequestObj: AmendRequest = AmendRequest("2019-04-06", "2020-04-05", "Bovis", "BV40092",
    Seq(
      PeriodDetails(355.00, "2019-06-06", "2019-07-05", Some(35.00), 1457.00),
      PeriodDetails(355.00, "2019-07-06", "2019-08-05", Some(35.00), 1457.00)
    )
  )

  val amendMissingOptionalRequestObj: AmendRequest = AmendRequest("2019-04-06", "2020-04-05", "Bovis", "BV40092",
    Seq(
      PeriodDetails(355.00, "2019-06-06", "2019-07-05", None, 1457.00),
      PeriodDetails(355.00, "2019-07-06", "2019-08-05", None, 1457.00)
    )
  )

  val amendResponseObj: AmendResponse = AmendResponse("S4636A77V5KB8625U")
}


