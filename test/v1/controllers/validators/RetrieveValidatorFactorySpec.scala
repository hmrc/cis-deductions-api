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

package v1.controllers.validators

import api.controllers.validators.Validator
import api.mocks.MockAppConfig
import api.models.domain.{Nino, Source}
import api.models.errors._
import support.UnitSpec
import v1.models.request.retrieve.RetrieveRequestData

class RetrieveValidatorFactorySpec extends UnitSpec with MockAppConfig {
  private implicit val correlationId: String = "1234"
  private val nino                           = "AA123456A"
  private val invalidNino                    = "GHFG197854"

  class SetUp extends MockAppConfig {
    val validatorFactory: RetrieveValidatorFactory = new RetrieveValidatorFactory(mockAppConfig)
    MockedAppConfig.minTaxYearCisDeductions.returns("2020")

    def validator(nino: String, fromDate: Option[String], toDate: Option[String], source: Option[String]): Validator[RetrieveRequestData] =
      validatorFactory.validator(nino, fromDate, toDate, source)

  }

  "running validation" should {
    "return no errors" when {
      "all query parameters are passed in the request" in new SetUp {
        val result: Either[ErrorWrapper, RetrieveRequestData] =
          validator(nino, Some("2019-04-06"), Some("2020-04-05"), Some("all")).validateAndWrapResult()
        result shouldBe Right(RetrieveRequestData(Nino(nino), "2019-04-06", "2020-04-05", Source.All))
      }

      "an optional field returns None" in new SetUp {
        val result: Either[ErrorWrapper, RetrieveRequestData] =
          validator(nino, Some("2019-04-06"), Some("2020-04-05"), None).validateAndWrapResult()
        result shouldBe Right(RetrieveRequestData(Nino(nino), "2019-04-06", "2020-04-05", Source.All))
      }
    }

    "return errors" when {
      "invalid nino and source data is passed in the request" in new SetUp {
        private val result = validator(invalidNino, Some("2019-04-06"), Some("2020-04-05"), Some("All")).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper("1234", BadRequestError, Some(List(NinoFormatError, RuleSourceInvalidError))))
      }

      "invalid dates are provided in the request" in new SetUp {
        private val result = validator(nino, Some("2018-04-06"), Some("2020-04-05"), Some("contractor")).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper("1234", RuleDateRangeInvalidError))
      }

      "the from and to date are not provided" in new SetUp {
        private val result = validator(nino, None, None, Some("customer")).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper("1234", BadRequestError, Some(List(RuleMissingToDateError, RuleMissingFromDateError))))
      }

      "the from & to date are not in the correct format" in new SetUp {
        private val result = validator(nino, Some("last week"), Some("this week"), Some("customer")).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper("1234", BadRequestError, Some(List(ToDateFormatError, FromDateFormatError))))
      }

      "the from date is not the start of the tax year" in new SetUp {
        private val result = validator(nino, Some("2019-04-05"), Some("2020-04-05"), Some("customer")).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper("1234", RuleDateRangeInvalidError))
      }

      "the to date is not the end of the tax year" in new SetUp {
        private val result = validator(nino, Some("2019-04-06"), Some("2020-04-04"), Some("customer")).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper("1234", RuleDateRangeInvalidError))
      }
    }
  }

}
