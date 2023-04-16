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
import tsec.common.VerificationStatus
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

final class LiveAuth[F[_]: Async] private (users: Users[F], authenticator: Authenticator[F])
    extends Auth[F] {

  private def checkPassword(user: User, password: String): F[Boolean] =
    BCrypt.checkpwBool[F](password, PasswordHash[BCrypt](user.hashedPassword))

  private def findUser(email: String): EitherT[F, AuthenticationError, User] =
    EitherT(users.find(email).map(_.toRight(UserNotFound(email))))

  private def validateUser(
      user: User,
      password: String
  ): EitherT[F, AuthenticationError, User] = EitherT {
    for {
      isValid <- checkPassword(user, password)
    } yield if isValid then Right(user) else Left(InvalidPassword)
  }

  override def login(email: String, password: String): F[Either[AuthenticationError, JWTToken]] =
    (for {
      eitherUser    <- findUser(email)
      validatedUser <- validateUser(eitherUser, password)
      token         <- EitherT.right(authenticator.create(validatedUser.email))
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
