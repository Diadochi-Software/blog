package tech.diadochi.core.fixtures

import cats.syntax.option.*
import tech.diadochi.core.users.{Role, User}

trait UserFixture {

  protected val nonExistentEmail = "not found"

  protected val validPassword     = "a hashed password"

  private val validPasswordHash = "$2a$10$cOnfEuIG3SsmNp3WpzHmTOOYk4DSfY8KAtWoxhpq0LM6RgKL76mrS"

  protected val JohnDoe: User = User(
    "john@doe.com",
    validPasswordHash,
    "John",
    "Doe",
    "CompanyTM".some,
    Role.ADMIN
  )

  protected val updatedPassword = "a new hashed password"

  protected val UpdatedJohnDoe: User = JohnDoe.copy(
    company = "Another CompanyTM".some,
    hashedPassword = "$2a$10$hHTuASLZ5NUnNetZLnmF9.B4pzrXaBNubF7HlLqOic12jZcoRAuiO"
  )

  protected val JaneDoe: User =
    User(
      "jane@doe.com",
      validPasswordHash,
      "John",
      "Doe",
      "CompanyTM".some,
      Role.AUTHOR
    )

  protected val NewUser: User =
    User(
      "new@user.com",
      validPasswordHash,
      "New",
      "User",
      "New Company".some,
      Role.READER
    )

}