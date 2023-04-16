package tech.diadochi.core.fixtures

import cats.syntax.option.*
import tech.diadochi.core.users.{Role, User}

trait UserFixture {

  protected val nonExistentEmail = "not found"

  protected val JohnDoe: User = User(
    "john@doe.com",
    "$2a$10$mvX89VIiN1BJIe7BJJ6jweQMDDcveZtNPZXtV/.3fljL6x3I1wy2K",
    "John",
    "Doe",
    "CompanyTM".some,
    Role.ADMIN
  )

  protected val UpdatedJohnDoe: User = JohnDoe.copy(
    company = "Another CompanyTM".some,
    hashedPassword = "$2a$10$ZqreSjof/R4duVWv8JnJwe6Ed.eU2CIQ8v5tkYnViAGE8pHGVxMDS"
  )

  protected val JaneDoe: User =
    User(
      "jane@doe.com",
      "$2a$10$XB5lNTTyHJVkGIDn5p8VueMhRrmotVkUVv9cQ5RiZkCGNPzGadnc6",
      "John",
      "Doe",
      "CompanyTM".some,
      Role.AUTHOR
    )

  protected val NewUser: User =
    User(
      "new@user.com",
      "$2a$10$1D4dFN028nX6GdlFWpnNbOHb1XTSBZu3p6xNhDHvn6ASbf7FRB9pq",
      "New",
      "User",
      "New Company".some,
      Role.READER
    )

}
