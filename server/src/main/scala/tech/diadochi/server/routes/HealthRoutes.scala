package tech.diadochi.server.routes

import cats.Monad
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.ember.server.*
import org.http4s.server.Router

class HealthRoutes[F[_]: Monad] private extends Http4sDsl[F] {

  private val healthRoute: HttpRoutes[F] = {
    HttpRoutes.of[F] { case GET -> Root =>
      Ok("Still Alive")
    }
  }

  val routes: HttpRoutes[F] = Router(
    "/health" -> healthRoute
  )

}

object HealthRoutes {
  def apply[F[_]: Monad]: HealthRoutes[F] = new HealthRoutes[F]
}
