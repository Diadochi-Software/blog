package tech.diadochi.auth.live

import cats.Applicative
import cats.data.EitherT
import cats.effect.kernel.Async
import cats.syntax.all.*
import tech.diadochi.auth.algebra.Auth
import tech.diadochi.auth.algebra.Auth.{Authenticator, JWTToken}
import tech.diadochi.auth.data.{NewPasswordInfo, UserForm}
import tech.diadochi.auth.errors.AuthenticationError
import tech.diadochi.auth.errors.AuthenticationError.*
import tech.diadochi.core.users.User
import tech.diadochi.repo.algebra.Users
import tsec.common.{VerificationFailed, VerificationStatus, Verified}
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

final class LiveAuth[F[_]: Async] private (users: Users[F], authenticator: Authenticator[F])
    extends Auth[F] {

  private def checkPassword(user: User, password: String) = EitherT[F, AuthenticationError, User] {
    BCrypt.checkpw[F](password, PasswordHash[BCrypt](user.hashedPassword)).map {
      case Verified           => Right(user)
      case VerificationFailed => Left(InvalidPassword)
    }
  }

  private def findUser(email: String): EitherT[F, AuthenticationError, User] =
    EitherT(users.find(email).map(_.toRight(UserNotFound(email))))

  override def login(email: String, password: String): F[Either[AuthenticationError, JWTToken]] =
    (for {
      user      <- findUser(email)
      validUser <- checkPassword(user, password)
      token     <- EitherT.right(authenticator.create(validUser.email))
    } yield token).value

  override def signup(form: UserForm): F[Option[User]] = ???

  override def changePassword(
      email: String,
      newPassword: NewPasswordInfo
  ): F[Either[AuthenticationError, User]] = ???

}

object LiveAuth {

  def apply[F[_]: Async](users: Users[F], authenticator: Authenticator[F]): F[Auth[F]] =
    new LiveAuth[F](users, authenticator).pure[F]

}
