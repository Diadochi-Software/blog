package tech.diadochi.server

import cats.effect.*
import cats.implicits.*
import cats.syntax.semigroup.*
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import tech.diadochi.repo.Data
import tech.diadochi.server.routes.{HealthRoutes, PostsRoutes}

class HttpApi[F[_]: Concurrent: Logger] private (data: Data[F]) {

  private val healthRoutes: HttpRoutes[F] = HealthRoutes[F].routes
  private val postsRoutes: HttpRoutes[F]  = PostsRoutes[F](data.posts).routes

  val endpoints: HttpRoutes[F] = Router(
    "/api" -> (healthRoutes <+> postsRoutes)
  )

}

object HttpApi {

  def apply[F[_]: Concurrent: Logger](data: Data[F]): Resource[F, HttpApi[F]] =
    Resource.pure(new HttpApi[F](data))

}
