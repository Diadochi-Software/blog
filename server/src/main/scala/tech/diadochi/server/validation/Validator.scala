package tech.diadochi.server.validation

import cats.*
import cats.data.*
import cats.data.Validated.*
import cats.syntax.validated.*
import tech.diadochi.server.validation.ValidationFailure.{EmptyField, InvalidEmail}
import tech.diadochi.server.validation.Validator.ValidationResult

trait Validator[A] {

  def validate(a: A): ValidationResult[A]

}

object Validator {

  def apply[A](implicit validator: Validator[A]): Validator[A] = validator

  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]

}
