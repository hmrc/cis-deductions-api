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

package api.hateoas

import api.hateoas.Method.{DELETE, GET, POST, PUT}
import api.hateoas.RelType._
import config.AppConfig
import shared.models.domain.TaxYear

trait HateoasLinks {

  private def withTaxYearParameter(appConfig: AppConfig, uri: String, maybeTaxYear: Option[TaxYear]): String = {

    maybeTaxYear match {
      case Some(taxYear) if taxYear.useTaxYearSpecificApi => s"$uri?taxYear=${taxYear.asMtd}"
      case _                                              => uri
    }
  }

  private def baseUri(appConfig: AppConfig, nino: String) =
    s"/${appConfig.apiGatewayContext}/$nino"

  private def retrieveUri(appConfig: AppConfig, nino: String, fromDate: String, toDate: String, source: Option[String]): String = {
    val sourceParam = if (!source.isDefined) "" else s"&source=${source.get}"
    s"${baseUri(appConfig, nino)}/current-position?fromDate=$fromDate&toDate=$toDate$sourceParam"
  }

  // API resource links
  // L1
  def createCisDeduction(appConfig: AppConfig, nino: String, isSelf: Boolean): Link =
    Link(href = baseUri(appConfig, nino) + s"/amendments", method = POST, rel = if (isSelf) SELF else CREATE_CIS)

  // L2
  def deleteCisDeduction(appConfig: AppConfig, nino: String, id: String, taxYear: Option[TaxYear], isSelf: Boolean): Link = {
    val uri = baseUri(appConfig, nino) + s"/amendments/$id"

    Link(href = withTaxYearParameter(appConfig, uri, taxYear), method = DELETE, rel = if (isSelf) SELF else DELETE_CIS)
  }

  // L3
  def amendCisDeduction(appConfig: AppConfig, nino: String, id: String, isSelf: Boolean): Link =
    Link(href = baseUri(appConfig, nino) + s"/amendments/$id", method = PUT, rel = if (isSelf) SELF else AMEND_CIS)

  // L4
  def retrieveCisDeduction(appConfig: AppConfig, nino: String, fromDate: String, toDate: String, source: Option[String], isSelf: Boolean): Link =
    Link(href = retrieveUri(appConfig, nino, fromDate, toDate, source), method = GET, rel = if (isSelf) SELF else RETRIEVE_CIS)

}
