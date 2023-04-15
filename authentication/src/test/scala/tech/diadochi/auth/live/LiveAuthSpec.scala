package tech.diadochi.auth.live

import cats.data.OptionT
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.http4s.{Request, Response}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import tech.diadochi.auth.algebra.Auth.Authenticator
import tech.diadochi.auth.data.{NewPasswordInfo, UserForm}
import tech.diadochi.auth.errors.ChangePasswordError.{InvalidPassword, UserNotFound}
import tech.diadochi.core.fixtures.UserFixture
import tech.diadochi.core.users.{Role, User}
import tech.diadochi.repo.algebra.Users
import tech.diadochi.repo.live.DoobieSpec
import tsec.authentication
import tsec.authentication.{AugmentedJWT, IdentityStore, JWTAuthenticator}
import tsec.mac.jca.HMACSHA256
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

import scala.concurrent.duration.*

class LiveAuthSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with UserFixture {

  import LiveAuthSpec.{*, given}

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  "Auth Algebra" - {
    "login should return None if the user doesn't exist" in {
      (for {
        auth       <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeToken <- auth.login("some@mail.com", "password")
      } yield maybeToken) asserting (_ shouldBe None)
    }
    "login should return None if the user exists but the password is wrong" in {
      (for {
        auth       <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeToken <- auth.login(JohnDoe.email, "password")
      } yield maybeToken) asserting (_ shouldBe None)
    }
    "login should return Some(token) if the user exists and the password is correct" in {
      (for {
        auth       <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeToken <- auth.login(JohnDoe.email, JohnDoe.hashedPassword)
      } yield maybeToken) asserting (_ shouldBe defined)
    }

    "signup should not create a user with an existing email" in {
      (for {
        auth      <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeUser <- auth.signup(JohnDoe)
      } yield maybeUser) asserting (_ shouldBe None)
    }
    "signup should create a user with a new email" in {
      (for {
        auth      <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeUser <- auth.signup(JaneDoe)
      } yield maybeUser) asserting (_ shouldBe Some(JaneDoe))
    }

    "changePassword should return Left[UserNotFound] if the user doesn't exist" in {
      (for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        res <- auth.changePassword(
          NewUser.email,
          NewPasswordInfo("oldPassword", "new password")
        )
      } yield res) asserting (_ shouldBe Left(UserNotFound(NewUser.email)))
    }
    "changePassword should return Left[InvalidPassword] if the password is incorrect" in {
      (for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        res <- auth.changePassword(
          JohnDoe.email,
          NewPasswordInfo("oldPassword", "new password")
        )
      } yield res) asserting (_ shouldBe Left(InvalidPassword))
    }
    "changePassword should correctly change password if all details are correct" in {
      (for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        res <- auth.changePassword(
          JohnDoe.email,
          NewPasswordInfo(JohnDoe.hashedPassword, "new password")
        )
        isNicePassword <- res match
          case Right(user) =>
            BCrypt.checkpwBool[IO]("new password", PasswordHash[BCrypt](user.hashedPassword))
          case Left(_) => IO pure false
      } yield isNicePassword) asserting (_ shouldBe true)
    }
  }

}

object LiveAuthSpec extends UserFixture {

  given userToForm: Conversion[User, UserForm] with {
    def apply(user: User): UserForm = UserForm(
      user.email,
      user.hashedPassword,
      user.firstName,
      user.lastName,
      Some("Company"),
      Role.READER
    )
  }

  private val mockedUsers = new Users[IO] {

    override def find(email: String): IO[Option[User]] =
      if email == JohnDoe.email then IO pure Some(JohnDoe) else IO pure None

    override def create(user: User): IO[String] = IO pure user.email

    override def update(user: User): IO[Option[User]] = IO pure Some(user)

    override def delete(email: String): IO[Boolean] = IO pure true

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