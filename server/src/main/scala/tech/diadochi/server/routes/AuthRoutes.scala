package tech.diadochi.server.routes

import cats.effect.*
import cats.implicits.*
import cats.syntax.semigroup.*
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.OptionalQueryParamDecoderMatcher
import org.http4s.server.Router
import org.http4s.{EntityDecoder, HttpRoutes}
import org.typelevel.log4cats.Logger
import tech.diadochi.auth.algebra.Auth
import tech.diadochi.core.posts.{Post, PostContent}
import tech.diadochi.repo.algebra.{Posts, Users}
import tech.diadochi.repo.filters.PostFilter
import tech.diadochi.repo.pagination.Pagination
import tech.diadochi.server.logging.syntax.*
import tech.diadochi.server.responses.FailureResponse
import tech.diadochi.server.validation.Validators.postContentValidator
import tech.diadochi.server.validation.syntax.HttpValidation

import java.time.LocalDateTime
import java.util.UUID
import scala.collection.mutable

class AuthRoutes[F[_]: Concurrent: Logger] private (authentication: Auth[F])
    extends Http4sDsl[F]
    with HttpValidation[F] {
  import AuthRoutes.*

  private val loginRoute: HttpRoutes[F] = ???

  private val signupRoute: HttpRoutes[F] = ???

  private val changePasswordRoute: HttpRoutes[F] = ???

  private val logoutRoute: HttpRoutes[F] = ???

  val routes: HttpRoutes[F] = Router(
    "/auth" -> (loginRoute <+> signupRoute <+> changePasswordRoute <+> logoutRoute)
  )

}

object AuthRoutes {

  def apply[F[_]: Concurrent: Logger](auth: Auth[F]): AuthRoutes[F] = new AuthRoutes[F](auth)

}
