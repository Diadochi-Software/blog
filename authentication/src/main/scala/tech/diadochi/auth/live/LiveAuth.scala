package tech.diadochi.auth.live

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import tech.diadochi.auth.algebra.Auth
import tech.diadochi.auth.algebra.Auth.{Authenticator, JWTToken}
import tech.diadochi.auth.data.UserForm
import tech.diadochi.core.users.User
import tech.diadochi.repo.algebra.Users

final class LiveAuth[F[_]] private (users: Users[F], authenticator: Authenticator[F])
    extends Auth[F] {

  override def login(email: String, password: String): F[Option[JWTToken]] = ???

  override def signup(email: String, password: String, userInfo: UserForm): F[Option[User]] = ???

  override def changePassword(email: String, newPassword: String): F[Either[String, User]] = ???

}

object LiveAuth {

  def apply[F[_]: Applicative](users: Users[F], authenticator: Authenticator[F]): F[Auth[F]] =
    new LiveAuth[F](users, authenticator).pure[F]

}
