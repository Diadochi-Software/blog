package tech.diadochi.auth.algebra

import cats.effect.Sync
import org.http4s.Uri.UserInfo
import tech.diadochi.auth.data.{NewPasswordInfo, UserForm}
import tech.diadochi.auth.errors.ChangePasswordError
import tech.diadochi.core.users.User
import tsec.authentication.{AugmentedJWT, JWTAuthenticator}
import tsec.mac.jca.HMACSHA256

trait Auth[F[_]] {

  import Auth.*

  def login(email: String, password: String): F[Option[JWTToken]]
  
  def signup(form: UserForm): F[Option[User]]

  def changePassword(
      email: String,
      newPassword: NewPasswordInfo
  ): F[Either[ChangePasswordError, User]]

}

object Auth {

  private[auth] type JWTToken            = AugmentedJWT[HMACSHA256, String]
  private[auth] type Authenticator[F[_]] = JWTAuthenticator[F, String, User, HMACSHA256]

}
