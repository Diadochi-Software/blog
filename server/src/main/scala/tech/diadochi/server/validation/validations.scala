package tech.diadochi.server.validation

import cats.syntax.all.*
import tech.diadochi.server.validation.ValidationFailure.{EmptyField, InvalidEmail}
import tech.diadochi.server.validation.Validator.ValidationResult

object validations {

  private val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  private[validation] def validateRequired[A](field: A, fieldName: String)(
      isEmpty: A => Boolean
  ): ValidationResult[A] =
    if (isEmpty(field)) EmptyField(fieldName).invalidNel else field.validNel

  private def validateEmailFormat(email: String): ValidationResult[String] = {
    if emailRegex matches email then email.validNel
    else InvalidEmail(email).invalidNel
  }

  private[validation] def validateEmail(email: String) =
    validateRequired(email, "email")(_.isEmpty) andThen validateEmailFormat

  private[validation] def validatePassword(password: String) =
    validateRequired(password, "password")(_.isEmpty) // TODO: add password logic

}
