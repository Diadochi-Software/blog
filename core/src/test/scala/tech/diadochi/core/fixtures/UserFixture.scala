package tech.diadochi.core.fixtures

import cats.syntax.option.*
import tech.diadochi.core.users.{Role, User}

trait UserFixture {

  protected val JohnDoe: User = User(
    "john@doe.com",
    "a hashed password",
    "John",
    "Doe",
    "CompanyTM".some,
    Role.ADMIN
  )

  protected val UpdatedJohnDoe: User = JohnDoe.copy(
    company = "Another CompanyTM".some,
    hashedPassword = "changed password"
  )

  protected val JaneDoe: User =
    User(
      "jane@doe.com",
      "another hashed password",
      "John",
      "Doe",
      "CompanyTM".some,
      Role.AUTHOR
    )

  protected val NewUser: User =
    User(
      "new@user.com",
      "a new password",
      "New",
      "User",
      "New Company".some,
      Role.READER
    )

}
