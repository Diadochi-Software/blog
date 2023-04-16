package tech.diadochi.fixtures

import tech.diadochi.auth.forms.{LoginForm, SignupForm}
import tech.diadochi.core.users.Role

trait FormFixture extends UserFixture {

  val JohnDoeLogin: LoginForm = LoginForm(
    "john@doe.com",
    validPassword
  )

  val NewUserForm: SignupForm = SignupForm(
    "new@user.com",
    validPassword,
    "New",
    "User",
    Some("New Company"),
    Role.READER
  )

  val JohnDoeSignup: SignupForm = SignupForm(
    "john@doe.com",
    validPassword,
    "John",
    "Doe",
    Some("CompanyTM"),
    Role.ADMIN
  )

}
