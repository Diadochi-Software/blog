package tech.diadochi.core.users

case class User(
    email: String,
    hashedPassword: String,
    firstName: String,
    lastName: String,
    company: Option[String],
    role: Role
)
