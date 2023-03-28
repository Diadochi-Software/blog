package tech.diadochi.blog

import cats.effect.*
import cats.effect.syntax.resource.*
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import tech.diadochi.blog.config.AppConfig
import tech.diadochi.blog.config.syntax.*
import tech.diadochi.repo.Data
import tech.diadochi.server.HttpApi

object Main extends ResourceApp.Forever {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def run(args: List[String]): Resource[IO, Unit] =
    ConfigSource.default.loadF[IO, AppConfig].toResource.flatMap {
      case AppConfig(postgres, ember) =>
        for {
          data <- Data[IO](postgres)
          api  <- HttpApi[IO](data)
          _ <- EmberServerBuilder
            .default[IO]
            .withHost(ember.host)
            .withPort(ember.port)
            .withHttpApp(api.endpoints.orNotFound)
            .build
        } yield ()
    }

}
