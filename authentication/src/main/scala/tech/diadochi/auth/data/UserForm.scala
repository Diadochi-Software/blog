package tech.diadochi.auth.data

import tech.diadochi.core.users.Role

final case class UserForm(
    firstName: Option[String],
    lastName: Option[String],
    company: Option[String],
    role: Role
)
