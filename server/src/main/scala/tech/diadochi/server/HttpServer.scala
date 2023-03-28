package tech.diadochi.server

import cats.Monad
import cats.effect.*
import cats.effect.syntax.resource.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.ember.server.*
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.Logger
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException
import tech.diadochi.repo.Data
import tech.diadochi.server.config.EmberConfig
import tech.diadochi.server.config.syntax.*
import tech.diadochi.server.routes.HealthRoutes

class HttpServer[F[_]: Async: Logger] {

  def start: Resource[F, Server] =
    for {
      config <- ConfigSource.default.loadF[F, EmberConfig].toResource
      data   <- Data[F]
      api    <- HttpApi[F](data)
      server <- EmberServerBuilder
        .default[F]
        .withHost(config.host)
        .withPort(config.port)
        .withHttpApp(api.endpoints.orNotFound)
        .build
    } yield server

}

object HttpServer {
  def apply[F[_]: Async: Logger]: HttpServer[F] = new HttpServer[F]
}
