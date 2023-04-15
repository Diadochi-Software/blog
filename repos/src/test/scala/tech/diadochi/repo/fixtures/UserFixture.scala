package tech.diadochi.repo.fixtures

import cats.syntax.option.*
import tech.diadochi.core.users.{Role, User}

trait UserFixture {

  protected val JohnDoe: User = User(
    "john@doe.com",
    "a hashed password",
    "John".some,
    "Doe".some,
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
      "John".some,
      "Doe".some,
      "CompanyTM".some,
      Role.AUTHOR
    )

  protected val NewUser: User =
    User(
      "new@user.com",
      "a new password",
      "New".some,
      "User".some,
      "New Company".some,
      Role.READER
    )

}
