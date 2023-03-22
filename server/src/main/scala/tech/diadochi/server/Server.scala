package tech.diadochi.server

import cats.Monad
import cats.effect.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.ember.server.*
import org.http4s.server.Router
import tech.diadochi.server.routes.HealthRoutes

object Server extends IOApp.Simple {

  override def run: IO[Unit] = EmberServerBuilder
    .default[IO]
    .withHttpApp(HealthRoutes[IO].routes.orNotFound)
    .build
    .use(_ => IO.println("Hit me up!") *> IO.never)

}
