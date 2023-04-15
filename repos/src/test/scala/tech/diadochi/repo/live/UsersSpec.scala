package tech.diadochi.repo.live

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.postgresql.util.PSQLException
import org.scalatest.Inside
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import tech.diadochi.repo.fixtures.{PostFixture, UserFixture}
import tech.diadochi.repo.live.DoobieSpec
import tech.diadochi.repo.pagination.Pagination

class UsersSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with Inside
    with DoobieSpec
    with UserFixture {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override val initScript: String = "sql/users.sql"

  "Users algebra" - {
    "should retrieve a user by email" in {
      transactor.use { xa =>
        (for {
          users     <- LiveUsers[IO](xa)
          retrieved <- users.find("john@doe.com")
        } yield retrieved) asserting (_ shouldBe Some(JohnDoe))
      }
    }
    "should return None if the email doesn't exist" in {
      transactor.use { xa =>
        (for {
          users     <- LiveUsers[IO](xa)
          retrieved <- users.find("not found")
        } yield retrieved) asserting (_ shouldBe None)
      }
    }
    "should create a new user" in {
      transactor.use { xa =>
        (for {
          users     <- LiveUsers[IO](xa)
          created   <- users.create(NewUser)
          retrieved <- users.find(NewUser.email)
        } yield (created, retrieved)).asserting { case (created, retrieved) =>
          created should be(NewUser.email)
          retrieved should be(Some(NewUser))
        }
      }
    }
    "should fail create a new user if the email already exists" in {
      transactor.use { xa =>
        (for {
          users   <- LiveUsers[IO](xa)
          created <- users.create(JohnDoe).attempt
        } yield created).asserting {
          inside(_) {
            case Left(error) => error shouldBe a[PSQLException]
            case _           => fail()
          }
        }
      }
    }
    "should return none when updating a user that doesn't exist" in {
      transactor.use { xa =>
        (for {
          users   <- LiveUsers[IO](xa)
          updated <- users.update(NewUser)
        } yield updated) asserting (_ shouldBe None)
      }
    }
    "should update an existing user" in {
      transactor.use { xa =>
        (for {
          users   <- LiveUsers[IO](xa)
          updated <- users.update(UpdatedJohnDoe)
        } yield updated) asserting (_ shouldBe Some(UpdatedJohnDoe))
      }
    }
    "should delete a user" in {
      transactor.use { xa =>
        (for {
          users   <- LiveUsers[IO](xa)
          updated <- users.delete(JohnDoe.email)
        } yield updated) asserting (_ shouldBe true)
      }
    }
    "should not delete a non existent user" in {
      transactor.use { xa =>
        (for {
          users   <- LiveUsers[IO](xa)
          updated <- users.delete("not found")
        } yield updated) asserting (_ shouldBe false)
      }
    }
  }

}
