package tech.diadochi.auth.data

import tech.diadochi.core.users.Role

final case class UserForm(
    email: String,
    password: String,
    firstName: String,
    lastName: String,
    company: Option[String],
    role: Role
)
