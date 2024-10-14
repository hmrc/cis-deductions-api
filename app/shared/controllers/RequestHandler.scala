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

package shared.controllers

import cats.data.EitherT
import cats.data.Validated.Valid
import cats.implicits._
import play.api.http.Status
import play.api.libs.json.{JsValue, Writes}
import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError
import shared.config.SharedAppConfig
import shared.config.Deprecation.Deprecated
import shared.controllers.validators.Validator
import shared.hateoas.{HateoasData, HateoasFactory, HateoasLinksFactory, HateoasWrapper}
import shared.models.errors.{ErrorWrapper, InternalError, RuleRequestCannotBeFulfilledError}
import shared.models.outcomes.ResponseWrapper
import shared.routing.Version
import shared.services.ServiceOutcome
import shared.utils.DateUtils.longDateTimestampGmt
import shared.utils.Logging

import scala.concurrent.{ExecutionContext, Future}

trait RequestHandler {
  def handleRequest()(implicit ctx: RequestContext, request: UserRequest[_], ec: ExecutionContext, appConfig: SharedAppConfig): Future[Result]
}

object RequestHandler {

  def withValidator[Input](validator: Validator[Input]): ValidatorOnlyBuilder[Input] =
    new ValidatorOnlyBuilder[Input](validator)

  class ValidatorOnlyBuilder[Input] private[RequestHandler] (validator: Validator[Input]) {

    def withService[Output](serviceFunction: Input => Future[ServiceOutcome[Output]]): RequestHandlerBuilder[Input, Output] =
      RequestHandlerBuilder(validator, serviceFunction)

  }

  case class RequestHandlerBuilder[Input, Output] private[RequestHandler] (
      validator: Validator[Input],
      service: Input => Future[ServiceOutcome[Output]],
      errorHandling: ErrorHandling = ErrorHandling.Default,
      resultCreator: ResultCreator[Input, Output] = ResultCreator.noContent[Input, Output](),
      auditHandler: Option[AuditHandler] = None,
      responseModifier: Option[Output => Output] = None
  ) extends RequestHandler {

    def handleRequest()(implicit ctx: RequestContext, request: UserRequest[_], ec: ExecutionContext, appConfig: SharedAppConfig): Future[Result] =
      Delegate.handleRequest()

    def withErrorHandling(errorHandling: ErrorHandling): RequestHandlerBuilder[Input, Output] =
      copy(errorHandling = errorHandling)

    def withAuditing(auditHandler: AuditHandler): RequestHandlerBuilder[Input, Output] =
      copy(auditHandler = Some(auditHandler))

    def withResponseModifier(responseModifier: Output => Output): RequestHandlerBuilder[Input, Output] =
      copy(responseModifier = Option(responseModifier))

    /** Shorthand for
      * {{{
      * withResultCreator(ResultCreator.plainJson(successStatus))
      * }}}
      */
    def withPlainJsonResult(successStatus: Int = Status.OK)(implicit ws: Writes[Output]): RequestHandlerBuilder[Input, Output] =
      withResultCreator(ResultCreator.plainJson(successStatus))

    /** Shorthand for
      * {{{
      * withResultCreator(ResultCreator.noContent)
      * }}}
      */
    def withNoContentResult(successStatus: Int = Status.NO_CONTENT): RequestHandlerBuilder[Input, Output] =
      withResultCreator(ResultCreator.noContent(successStatus))

    def withResultCreator(resultCreator: ResultCreator[Input, Output]): RequestHandlerBuilder[Input, Output] =
      copy(resultCreator = resultCreator)

    /** Shorthand for
      * {{{
      * withResultCreator(ResultCreator.hateoasWrapping(hateoasFactory, successStatus)(data))
      * }}}
      */
    def withHateoasResultFrom[HData <: HateoasData](
        hateoasFactory: HateoasFactory)(data: (Input, Output) => HData, successStatus: Int = Status.OK)(implicit
        linksFactory: HateoasLinksFactory[Output, HData],
        writes: Writes[HateoasWrapper[Output]]): RequestHandlerBuilder[Input, Output] =
      withResultCreator(ResultCreator.hateoasWrapping(hateoasFactory, successStatus)(data))

    /** Shorthand for
      * {{{
      * withResultCreator(ResultCreator.hateoasWrapping(hateoasFactory, successStatus)((_,_) => data))
      * }}}
      */
    def withHateoasResult[HData <: HateoasData](hateoasFactory: HateoasFactory)(data: HData, successStatus: Int = Status.OK)(implicit
        linksFactory: HateoasLinksFactory[Output, HData],
        writes: Writes[HateoasWrapper[Output]]): RequestHandlerBuilder[Input, Output] =
      withResultCreator(ResultCreator.hateoasWrapping(hateoasFactory, successStatus)((_, _) => data))

    // Scoped as a private delegate so as to keep the logic completely separate from the configuration
    private object Delegate extends RequestHandler with Logging with RequestContextImplicits {

      implicit class Response(result: Result)(implicit appConfig: SharedAppConfig, apiVersion: Version) {

        private def withDeprecationHeaders: List[(String, String)] = {

          appConfig.deprecationFor(apiVersion) match {
            case Valid(Deprecated(deprecatedOn, Some(sunsetDate))) =>
              List(
                "Deprecation" -> longDateTimestampGmt(deprecatedOn),
                "Sunset"      -> longDateTimestampGmt(sunsetDate),
                "Link"        -> appConfig.apiDocumentationUrl
              )
            case Valid(Deprecated(deprecatedOn, None)) =>
              List(
                "Deprecation" -> longDateTimestampGmt(deprecatedOn),
                "Link"        -> appConfig.apiDocumentationUrl
              )
            case _ => Nil
          }
        }

        def withApiHeaders(correlationId: String, responseHeaders: (String, String)*): Result = {

          val headers =
            responseHeaders ++
              List(
                "X-CorrelationId"        -> correlationId,
                "X-Content-Type-Options" -> "nosniff"
              ) ++
              withDeprecationHeaders

          result.copy(header = result.header.copy(headers = result.header.headers ++ headers))
        }

      }

      def handleRequest()(implicit
          ctx: RequestContext,
          request: UserRequest[_],
          ec: ExecutionContext,
          appConfig: SharedAppConfig
      ): Future[Result] = {

        logger.info(
          message = s"[${ctx.endpointLogContext.controllerName}][${ctx.endpointLogContext.endpointName}] " +
            s"with correlationId : ${ctx.correlationId}")

        val result =
          if (simulateRequestCannotBeFulfilled) {
            EitherT[Future, ErrorWrapper, Result](Future.successful(Left(ErrorWrapper(ctx.correlationId, RuleRequestCannotBeFulfilledError))))
          } else {
            for {
              parsedRequest   <- EitherT.fromEither[Future](validator.validateAndWrapResult())
              serviceResponse <- EitherT(service(parsedRequest))
            } yield doWithContext(ctx.withCorrelationId(serviceResponse.correlationId)) { implicit ctx: RequestContext =>
              responseModifier match {
                case Some(modifier) =>
                  handleSuccess(parsedRequest, serviceResponse.copy(responseData = modifier(serviceResponse.responseData)))
                case None =>
                  handleSuccess(parsedRequest, serviceResponse)
              }
            }
          }

        result.leftMap { errorWrapper =>
          doWithContext(ctx.withCorrelationId(errorWrapper.correlationId)) { implicit ctx: RequestContext =>
            handleFailure(errorWrapper)
          }
        }.merge
      }

      private def simulateRequestCannotBeFulfilled(implicit request: UserRequest[_], appConfig: SharedAppConfig): Boolean =
        request.headers.get("Gov-Test-Scenario").contains("REQUEST_CANNOT_BE_FULFILLED") &&
          appConfig.allowRequestCannotBeFulfilledHeader(Version(request))

      private def doWithContext[A](ctx: RequestContext)(f: RequestContext => A): A = f(ctx)

      private def handleSuccess(parsedRequest: Input, serviceResponse: ResponseWrapper[Output])(implicit
          ctx: RequestContext,
          request: UserRequest[_],
          ec: ExecutionContext,
          appConfig: SharedAppConfig): Result = {

        implicit val apiVersion: Version = Version(request)

        logger.info(
          s"[${ctx.endpointLogContext.controllerName}][${ctx.endpointLogContext.endpointName}] - " +
            s"Success response received with CorrelationId: ${ctx.correlationId}")

        val resultWrapper = resultCreator
          .createResult(parsedRequest, serviceResponse.responseData)

        val result = resultWrapper.asResult.withApiHeaders(ctx.correlationId)
        auditIfRequired(result.header.status, Right(resultWrapper.body))
        result
      }

      private def handleFailure(errorWrapper: ErrorWrapper)(implicit
          ctx: RequestContext,
          request: UserRequest[_],
          ec: ExecutionContext,
          appConfig: SharedAppConfig): Result = {

        implicit val apiVersion: Version = Version(request)
        logger.warn(
          s"[${ctx.endpointLogContext.controllerName}][${ctx.endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: ${ctx.correlationId}")

        val errorResult = errorHandling.errorHandler.applyOrElse(errorWrapper, unhandledError)
        val result      = errorResult.withApiHeaders(ctx.correlationId)
        auditIfRequired(result.header.status, Left(errorWrapper))
        result
      }

      private def unhandledError(errorWrapper: ErrorWrapper)(implicit endpointLogContext: EndpointLogContext): Result = {
        logger.error(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Unhandled error: $errorWrapper")
        InternalServerError(InternalError.asJson)
      }

      private def auditIfRequired(httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]])(implicit
          ctx: RequestContext,
          request: UserRequest[_],
          ec: ExecutionContext): Unit =
        auditHandler.foreach { creator =>
          creator.performAudit(request.userDetails, httpStatus, response)
        }

    }

  }

}
