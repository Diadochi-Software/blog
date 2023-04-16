package tech.diadochi.auth.errors

trait AuthenticationError extends Throwable

object AuthenticationError {

  final case class UserNotFound(email: String)      extends AuthenticationError
  final case class UserAlreadyExists(email: String) extends AuthenticationError
  case object InvalidPassword                       extends AuthenticationError

}
