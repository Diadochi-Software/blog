package tech.diadochi.server.validation

import io.circe.*
import io.circe.generic.AutoDerivation
import io.circe.syntax.*

sealed trait ValidationFailure(val errorMessage: String) extends AutoDerivation

object ValidationFailure {

  case class EmptyField(fieldName: String)
      extends ValidationFailure(s"Field $fieldName is required")

  case class InvalidLanguageFormat(language: String)
      extends ValidationFailure(
        s"Invalid language format: $language is not compliant with ISO 639-1"
      )

  case class InvalidEmail(email: String)
      extends ValidationFailure(s"Invalid email format: $email")

}
