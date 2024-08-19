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

package config

import play.api.Configuration
import shared.config.FeatureSwitches
import shared.models.domain.TaxYear
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

/** Put API-specific config here...
  */
@Singleton
class CisDeductionApiConfig @Inject() (config: ServicesConfig, configuration: Configuration) {

  def featureSwitchConfig: Configuration = configuration.getOptional[Configuration](s"feature-switch").getOrElse(Configuration.empty)

  def featureSwitches: FeatureSwitches = CisDeductionsApiFeatureSwitches(featureSwitchConfig)

  def minTaxYearCisDeductions: TaxYear = TaxYear.starting(config.getInt("minTaxYearCisDeductions"))

}
