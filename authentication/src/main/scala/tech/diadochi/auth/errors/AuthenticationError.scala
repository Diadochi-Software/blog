package tech.diadochi.auth.errors

import io.circe.Codec

sealed trait AuthenticationError(val error: String)

object AuthenticationError {

  final case class UserNotFound(email: String)
      extends AuthenticationError(s"User with email $email not found")
  final case class UserAlreadyExists(email: String)
      extends AuthenticationError(s"User with email $email already exists")
  case object InvalidPassword extends AuthenticationError("Invalid password")

}
