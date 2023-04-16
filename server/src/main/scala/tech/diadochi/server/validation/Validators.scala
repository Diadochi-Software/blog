package tech.diadochi.server.validation

import cats.*
import cats.data.Validated.*
import cats.implicits.*
import tech.diadochi.auth.forms.{ChangePasswordForm, LoginForm, SignupForm}
import tech.diadochi.core.posts.PostContent
import tech.diadochi.server.validation.validations.{validateEmail, validatePassword, validateRequired}

import java.util.UUID

object Validators {

  given postContentValidator: Validator[PostContent] = new PostContentValidator

  given loginFormValidator: Validator[LoginForm] = { a =>
    (
      validateEmail(a.email),
      validateRequired(a.password, "password")(_.isEmpty)
    ).mapN(LoginForm.apply)
  }

  given signupFormValidator: Validator[SignupForm] = { a =>
    (
      validateEmail(a.email),
      validatePassword(a.password),
      validateRequired(a.firstName, "first name")(_.isEmpty),
      validateRequired(a.lastName, "last name")(_.isEmpty),
      a.company.validNel,
      a.role.validNel
    ).mapN(SignupForm.apply)
  }

  given changePasswordFormValidator: Validator[ChangePasswordForm] = { a =>
    (
      validateRequired(a.oldPassword, "old password")(_.isEmpty),
      validatePassword(a.newPassword)
    ).mapN(ChangePasswordForm.apply)
  }

}
