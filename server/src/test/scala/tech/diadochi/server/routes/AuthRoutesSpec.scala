package tech.diadochi.server.routes

import cats.data.OptionT
import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits.*
import cats.syntax.semigroup.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.*
import org.http4s.headers.Authorization
import org.http4s.implicits.*
import org.http4s.server.Router
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.ci.CIStringSyntax
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import tech.diadochi.auth.algebra.Auth
import tech.diadochi.auth.algebra.Auth.{Authenticator, JWTToken}
import tech.diadochi.auth.errors.AuthenticationError
import tech.diadochi.auth.errors.AuthenticationError.{InvalidPassword, UserAlreadyExists, UserNotFound}
import tech.diadochi.auth.fixtures.FormFixture
import tech.diadochi.auth.forms.{ChangePasswordForm, LoginForm, SignupForm}
import tech.diadochi.core.fixtures.UserFixture
import tech.diadochi.core.posts.{Post, PostContent}
import tech.diadochi.core.users.User
import tech.diadochi.repo.algebra.{PostContents, Posts}
import tech.diadochi.repo.filters.PostFilter
import tech.diadochi.repo.pagination.Pagination
import tech.diadochi.server.logging.syntax.*
import tech.diadochi.server.responses.FailureResponse
import tsec.authentication.{IdentityStore, JWTAuthenticator}
import tsec.jws.mac.JWTMac
import tsec.mac.jca.HMACSHA256

import java.util.UUID
import scala.concurrent.duration.*

class AuthRoutesSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with Http4sDsl[IO] {
  import AuthRoutesSpec.{*, given}

  private val authRoutes: HttpRoutes[IO] = AuthRoutes[IO](mockedAuth).routes

  "Auth Routes" - {

    "login should return a 401 - unauthorized if failed" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request[IO](Method.POST, uri"/auth/login")
            .withEntity(
              LoginForm(
                "test",
                "test"
              )
            )
        )
      } yield response.status shouldBe Status.Unauthorized
    }
    "login should return a 200 - ok + JWT token if successful" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request[IO](Method.POST, uri"/auth/login")
            .withEntity(
              LoginForm(
                JohnDoeLogin.email,
                JohnDoeLogin.password
              )
            )
        )
      } yield {
        response.status shouldBe Status.Ok
        response.headers.get(ci"Authorization") shouldBe defined
      }
    }

    "signup should return 400 - bad request if the user already exists" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request[IO](Method.POST, uri"/auth/signup")
            .withEntity(
              JohnDoeSignup
            )
        )

      } yield {
        response.status shouldBe Status.BadRequest
      }
    }
    "signup should return 201 - created if signup is successful" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request[IO](Method.POST, uri"/auth/signup")
            .withEntity(NewUserForm)
        )
      } yield {
        response.status shouldBe Status.Created
      }
    }

    "logout should return 200 - ok if logging out with a JWT token" in {
      for {
        jwtToken <- mockedAuthenticator.create(JohnDoe.email)
        response <- authRoutes.orNotFound.run(
          Request[IO](Method.POST, uri"/auth/logout")
            .withBearerToken(jwtToken)
        )
      } yield {
        response.status shouldBe Status.Ok
      }
    }
    "logout should return 401 - unauthorized if logging out without a JWT token" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request[IO](Method.POST, uri"/auth/logout")
        )
      } yield {
        response.status shouldBe Status.Unauthorized
      }
    }

    "changePassword should return 404 - not found if user doesn't exist" in {
      for {
        jwtToken <- mockedAuthenticator.create(JaneDoe.email)
        response <- authRoutes.orNotFound.run(
          Request[IO](Method.POST, uri"/auth/change-password")
            .withBearerToken(jwtToken)
            .withEntity(
              ChangePasswordForm(
                validPassword,
                "another password"
              )
            )
        )
      } yield {
        response.status shouldBe Status.NotFound
      }
    }
    "changePassword should return 403 - forbidden if the old password is incorrect" in {
      for {
        jwtToken <- mockedAuthenticator.create(JohnDoe.email)
        response <- authRoutes.orNotFound.run(
          Request[IO](Method.POST, uri"/auth/change-password")
            .withBearerToken(jwtToken)
            .withEntity(
              ChangePasswordForm(
                "wrong password",
                "another password"
              )
            )
        )
      } yield {
        response.status shouldBe Status.Forbidden
      }
    }
    "changePassword should return 401 - unauthorized if the JWT token is invalid" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request[IO](Method.POST, uri"/auth/change-password")
            .withEntity(
              ChangePasswordForm(
                validPassword,
                updatedPassword
              )
            )
        )
      } yield {
        response.status shouldBe Status.Unauthorized
      }
    }
    "changePassword should return 200 - ok if the password is changed successfully" in {
      for {
        jwtToken <- mockedAuthenticator.create(JohnDoe.email)
        response <- authRoutes.orNotFound.run(
          Request[IO](Method.POST, uri"/auth/change-password")
            .withBearerToken(jwtToken)
            .withEntity(
              ChangePasswordForm(
                validPassword,
                updatedPassword
              )
            )
        )
      } yield {
        response.status shouldBe Status.Ok
      }
    }

  }

}

object AuthRoutesSpec extends FormFixture {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  extension (r: Request[IO])
    def withBearerToken(token: JWTToken): Request[IO] =
      r.putHeaders {
        val jwtString = JWTMac.toEncodedString[IO, HMACSHA256](token.jwt)
        Authorization(Credentials.Token(AuthScheme.Bearer, jwtString))
      }

  private val mockedAuth = new Auth[IO] {

    override def login(email: String, password: String): IO[Either[AuthenticationError, JWTToken]] =
      if email == JohnDoe.email && password == validPassword then
        mockedAuthenticator.create(email).map(Right(_))
      else IO.pure(Left(InvalidPassword))

    override def signup(form: SignupForm): IO[Either[AuthenticationError, User]] =
      if form.email == NewUserForm.email then IO.pure(Right(NewUser))
      else IO.pure(Left(UserAlreadyExists(form.email)))

    override def changePassword(
        email: String,
        newPassword: ChangePasswordForm
    ): IO[Either[AuthenticationError, User]] =
      if email == JohnDoe.email then
        if newPassword.oldPassword == validPassword then IO.pure(Right(UpdatedJohnDoe))
        else IO.pure(Left(InvalidPassword))
      else IO.pure(Left(UserNotFound(email)))

    override def authenticator: Authenticator[IO] = mockedAuthenticator

  }

  private val mockedAuthenticator = {

    val key = HMACSHA256.unsafeGenerateKey

    val idStore: IdentityStore[IO, String, User] = (email: String) =>
      if email == JohnDoe.email then OptionT.pure(JohnDoe)
      else if email == JaneDoe.email then OptionT.pure(JaneDoe)
      else OptionT.none

    JWTAuthenticator.unbacked.inBearerToken(1.day, None, idStore, key)
  }

}
