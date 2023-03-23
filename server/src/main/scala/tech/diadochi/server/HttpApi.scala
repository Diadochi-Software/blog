package tech.diadochi.server

import cats.effect.*
import cats.implicits.*
import cats.syntax.semigroup.*
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import tech.diadochi.server.routes.{HealthRoutes, PostsRoutes}

class HttpApi[F[_]: Concurrent: Logger] {

  private val healthRoutes: HttpRoutes[F] = HealthRoutes[F].routes
  private val postsRoutes: HttpRoutes[F]  = PostsRoutes[F].routes

  val endpoints: HttpRoutes[F] = Router(
    "/api" -> (healthRoutes <+> postsRoutes)
  )

}

object HttpApi {

  def apply[F[_]: Concurrent: Logger]: HttpApi[F] = new HttpApi[F]

}
