package tech.diadochi.auth.forms

import tech.diadochi.core.users.Role

final case class SignupForm(
    email: String,
    password: String,
    firstName: String,
    lastName: String,
    company: Option[String],
    role: Role
)
