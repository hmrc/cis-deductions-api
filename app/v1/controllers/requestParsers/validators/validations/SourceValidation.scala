package v1.controllers.requestParsers.validators.validations

import v1.models.errors.{MtdError, RuleSourceError}

object SourceValidation {

  private val sources = Seq("all","customer","contractor")

  def validate(source: Option[String]): List[MtdError] = {
    source match{
      case Some(_) if sources.contains(_) => NoValidationErrors
      case None => NoValidationErrors
      case _ => List(RuleSourceError)
    }
  }
}
