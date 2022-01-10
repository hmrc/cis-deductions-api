/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators.validations

import play.api.libs.json.JsValue
import v1.models.errors.MtdError

object PeriodDataPositiveAmountValidation {

  private val minValue = BigDecimal(0)
  private val maxValue = BigDecimal(99999999999.99)

  def validate(json: JsValue, fieldName: String, error: MtdError): List[MtdError] = {

    val periodData = (json \ "periodData").as[List[JsValue]]

    periodData.map {
      period => amountValidation(period, fieldName, error)
    }.filter(_.isDefined).map(_.get)

  }

  private def amountValidation(json: JsValue, fieldName: String, error: MtdError): Option[MtdError] = {
    val amount = (json \ fieldName).asOpt[BigDecimal]

    amount match {
      case Some(value) if value > maxValue || value < minValue  => Some(error)
      case _ => None
    }

  }
}

