package tech.diadochi.auth.live

import cats.Applicative
import cats.data.EitherT
import cats.effect.kernel.Async
import cats.syntax.all.*
import tech.diadochi.auth.algebra.Auth
import tech.diadochi.auth.algebra.Auth.{Authenticator, JWTToken}
import tech.diadochi.auth.errors.AuthenticationError
import tech.diadochi.auth.errors.AuthenticationError.*
import tech.diadochi.auth.forms.{ChangePasswordForm, SignupForm}
import tech.diadochi.core.users.{Role, User}
import tech.diadochi.repo.algebra.Users
import tsec.common.{VerificationFailed, VerificationStatus, Verified}
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

final class LiveAuth[F[_]: Async] private (
    users: Users[F],
    override val authenticator: Authenticator[F]
) extends Auth[F] {

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

  private def validateForm(form: SignupForm): EitherT[F, AuthenticationError, User] =
    EitherT {
      for {
        maybeUser <- users.find(form.email)
        user <- maybeUser match {
          case Some(_) => AuthenticationError.UserAlreadyExists(form.email).asLeft[User].pure[F]
          case None =>
            for {
              hashedPassword <- BCrypt.hashpw[F](form.password)
            } yield User(
              form.email,
              hashedPassword,
              form.firstName,
              form.lastName,
              form.company,
              Role.READER
            ).asRight[AuthenticationError]
        }
      } yield user
    }

  override def signup(form: SignupForm): F[Either[AuthenticationError, User]] =
    (for {
      user <- validateForm(form)
      _    <- EitherT.right(users.create(user))
    } yield user).value

  private def updatePassword(
      user: User,
      newPassword: String
  ): EitherT[F, AuthenticationError, User] =
    for {
      hashedPassword <- EitherT.right(BCrypt.hashpw[F](newPassword))
      updatedUser = user.copy(hashedPassword = hashedPassword)
      _ <- EitherT.right(users.update(updatedUser))
    } yield updatedUser

  override def changePassword(
      email: String,
      newPassword: ChangePasswordForm
  ): F[Either[AuthenticationError, User]] =
    (for {
      user        <- findUser(email)
      validUser   <- checkPassword(user, newPassword.oldPassword)
      updatedUser <- updatePassword(validUser, newPassword.newPassword)
    } yield updatedUser).value

}

object LiveAuth {

  def apply[F[_]: Async](users: Users[F], authenticator: Authenticator[F]): F[Auth[F]] =
    new LiveAuth[F](users, authenticator).pure[F]

}
