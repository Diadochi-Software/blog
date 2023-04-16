package tech.diadochi.core.fixtures

import cats.syntax.option.*
import tech.diadochi.core.users.{Role, User}

trait UserFixture {

  val nonExistentEmail = "not found"

  val validPassword = "a hashed password"

  val validPasswordHash = "$2a$10$cOnfEuIG3SsmNp3WpzHmTOOYk4DSfY8KAtWoxhpq0LM6RgKL76mrS"

  val JohnDoe: User = User(
    "john@doe.com",
    validPasswordHash,
    "John",
    "Doe",
    "CompanyTM".some,
    Role.ADMIN
  )

  val updatedPassword = "a new hashed password"

  val UpdatedJohnDoe: User = JohnDoe.copy(
    company = "Another CompanyTM".some,
    hashedPassword = "$2a$10$hHTuASLZ5NUnNetZLnmF9.B4pzrXaBNubF7HlLqOic12jZcoRAuiO"
  )

  val JaneDoe: User =
    User(
      "jane@doe.com",
      validPasswordHash,
      "John",
      "Doe",
      "CompanyTM".some,
      Role.AUTHOR
    )

  val NewUser: User =
    User(
      "new@user.com",
      validPasswordHash,
      "New",
      "User",
      "New Company".some,
      Role.READER
    )

}
