package tech.diadochi.server.validation

import cats.*
import cats.data.{NonEmptyList, Validated}
import cats.effect.IO
import cats.implicits.*
import io.circe.generic.semiauto.*
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.*
import org.typelevel.log4cats.Logger
import tech.diadochi.server.logging.syntax.*

object syntax {

  trait HttpValidation[F[_]: MonadThrow: Logger] {
    self: Http4sDsl[F] =>

    extension (req: Request[F]) {
      def validate[A: Validator](
          serverLogicIfValid: A => F[Response[F]]
      )(using entityDecoder: EntityDecoder[F, A]): F[Response[F]] =
        req
          .as[A]
          .logError(ex => s"Failed to parse request body: ${ex.getMessage}")
          .map(Validator[A].validate)
          .flatMap {
            case Validated.Valid(a)   => serverLogicIfValid(a)
            case Validated.Invalid(e) => BadRequest(e.toList.map(_.errorMessage))
          }
    }
  }
}
