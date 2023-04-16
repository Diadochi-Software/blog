package tech.diadochi.auth.algebra

import cats.effect.Sync
import org.http4s.Response
import org.http4s.Uri.UserInfo
import tech.diadochi.auth.errors.AuthenticationError
import tech.diadochi.auth.forms.{ChangePasswordForm, SignupForm}
import tech.diadochi.core.users.User
import tsec.authentication.{AugmentedJWT, JWTAuthenticator, SecuredRequest}
import tsec.mac.jca.HMACSHA256

trait Auth[F[_]] {

  import Auth.*

  def login(email: String, password: String): F[Either[AuthenticationError, JWTToken]]

  def signup(form: SignupForm): F[Either[AuthenticationError, User]]

  def changePassword(
      email: String,
      newPassword: ChangePasswordForm
  ): F[Either[AuthenticationError, User]]

  def authenticator: Authenticator[F]

}

object Auth {

  type JWTToken            = AugmentedJWT[HMACSHA256, String]
  type Authenticator[F[_]] = JWTAuthenticator[F, String, User, HMACSHA256]
  type AuthRoute[F[_]]     = PartialFunction[SecuredRequest[F, User, JWTToken], F[Response[F]]]

}
