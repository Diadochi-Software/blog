package tech.diadochi.repo.live

import cats.effect.*
import doobie.*
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.util.*
import org.testcontainers.containers.PostgreSQLContainer
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import tech.diadochi.core.Post

trait DoobieSpec {

  val initScript: String

  val postgres: Resource[IO, PostgreSQLContainer[Nothing]] = {
    val acquire = IO {
      val container: PostgreSQLContainer[Nothing] =
        new PostgreSQLContainer("postgres").withInitScript(initScript)
      container.start()
      container
    }
    val release = (container: PostgreSQLContainer[Nothing]) => IO(container.stop())
    Resource.make(acquire)(release)
  }

  val transactor: Resource[IO, Transactor[IO]] =
    for {
      db <- postgres
      ce <- ExecutionContexts.fixedThreadPool[IO](1)
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver",
        db.getJdbcUrl,
        db.getUsername,
        db.getPassword,
        ce
      )
    } yield xa

}
