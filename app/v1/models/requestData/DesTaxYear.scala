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

package v1.models.requestData

/**
  * Represents a tax year for DES
  *
  * @param value the tax year string (where 2018 represents 2017-18)
  */
case class DesTaxYear(value: String) extends AnyVal {
  override def toString: String = value
}

object DesTaxYear {

  val taxYearStart: Int = 2
  val taxYearEnd: Int = 5

  /**
    * @param taxYear tax year in MTD format (e.g. 2017-18)
    */
  def fromMtd(taxYear: String): DesTaxYear =
    DesTaxYear(taxYear.take(taxYearStart) + taxYear.drop(taxYearEnd))
}
