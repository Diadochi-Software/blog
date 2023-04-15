package tech.diadochi.auth.errors

trait ChangePasswordError

object ChangePasswordError {

  case class UserNotFound(email: String) extends ChangePasswordError
  case object InvalidPassword            extends ChangePasswordError

}
