package tech.diadochi.server

import cats.Monad
import cats.effect.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.ember.server.*
import org.http4s.server.{Router, Server}
import cats.effect.syntax.resource._
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException
import tech.diadochi.server.config.EmberConfig
import tech.diadochi.server.config.syntax.*
import tech.diadochi.server.routes.HealthRoutes

object HttpServer {

  private def build(config: EmberConfig) =
    EmberServerBuilder
      .default[IO]
      .withHost(config.host)
      .withPort(config.port)
      .withHttpApp(HealthRoutes[IO].routes.orNotFound)
      .build

  def start: Resource[IO, Server] = for {
    config <- ConfigSource.default.loadF[IO, EmberConfig].toResource
    server <- build(config)
  } yield server

}
