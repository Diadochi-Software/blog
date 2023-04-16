package tech.diadochi.server.routes

import cats.effect.*
import cats.implicits.*
import cats.syntax.semigroup.*
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.OptionalQueryParamDecoderMatcher
import org.http4s.server.Router
import org.http4s.{EntityDecoder, HttpRoutes, Response, Status}
import org.typelevel.log4cats.Logger
import tech.diadochi.auth.algebra.Auth
import tech.diadochi.auth.algebra.Auth.{AuthRoute, JWTToken}
import tech.diadochi.auth.errors.AuthenticationError.{InvalidPassword, UserNotFound}
import tech.diadochi.auth.forms.{ChangePasswordForm, LoginForm, SignupForm}
import tech.diadochi.core.posts.{Post, PostContent}
import tech.diadochi.core.users.User
import tech.diadochi.repo.algebra.{Posts, Users}
import tech.diadochi.repo.filters.PostFilter
import tech.diadochi.repo.pagination.Pagination
import tech.diadochi.server.logging.syntax.*
import tech.diadochi.server.responses.FailureResponse
import tech.diadochi.server.validation.Validators.given
import tech.diadochi.server.validation.syntax.HttpValidation
import tsec.authentication.{SecuredRequestHandler, TSecAuthService, asAuthed}

import java.time.LocalDateTime
import java.util.UUID
import scala.collection.mutable

class AuthRoutes[F[_]: Concurrent: Logger] private (auth: Auth[F])
    extends Http4sDsl[F]
    with HttpValidation[F] {
  import AuthRoutes.*

  private val authenticator = auth.authenticator

  private val loginRoute: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root / "login" =>
    req.validate[LoginForm] { loginInfo =>
      for {
        _            <- Logger[F].info(s"Login attempt for user ${loginInfo.email}")
        errorOrToken <- auth.login(loginInfo.email, loginInfo.password)
      } yield errorOrToken match
        case Right(token)    => authenticator.embed(Response(Status.Ok), token)
        case Left(authError) => Response(Status.Unauthorized).withEntity(authError)
    }
  }

  private val signupRoute: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root / "signup" =>
    req.validate[SignupForm] { signupInfo =>
      for {
        _              <- Logger[F].info(s"Signup attempt for user ${signupInfo.email}")
        errorOrNewUser <- auth.signup(signupInfo)
        res <- errorOrNewUser match
          case Right(user)     => Created(user.email)
          case Left(authError) => BadRequest(authError)
      } yield res
    }
  }

  private val changePasswordRoute: AuthRoute[F] = {
    case req @ POST -> Root / "change-password" asAuthed user =>
      req.request.validate[ChangePasswordForm] { changePasswordInfo =>
        for {
          _              <- Logger[F].info(s"Change password attempt for user ${user.email}")
          errorOrNewUser <- auth.changePassword(user.email, changePasswordInfo)
          res <- errorOrNewUser match
            case Right(user)             => Ok(user.email)
            case Left(err: UserNotFound) => NotFound(err)
            case Left(InvalidPassword)   => Forbidden(InvalidPassword)
            case Left(err)               => BadRequest(err)
        } yield res
      }
  }

  private val logoutRoute: AuthRoute[F] = { case req @ POST -> Root / "logout" asAuthed _ =>
    val token = req.authenticator
    for {
      _    <- authenticator.discard(token)
      resp <- Ok()
    } yield resp
  }

  private val unauthedRoutes = loginRoute <+> signupRoute

  private val securedHandler: SecuredRequestHandler[F, String, User, JWTToken] =
    SecuredRequestHandler(authenticator)
  private val authedRoutes =
    securedHandler.liftService(TSecAuthService(changePasswordRoute orElse logoutRoute))

  val routes: HttpRoutes[F] = Router(
    "/auth" -> (unauthedRoutes <+> authedRoutes)
  )

}

object AuthRoutes {

  def apply[F[_]: Concurrent: Logger](auth: Auth[F]): AuthRoutes[F] = new AuthRoutes[F](auth)

}
