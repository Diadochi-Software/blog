package tech.diadochi.server.routes

import cats.effect.*
import cats.implicits.*
import cats.syntax.semigroup.*
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{EntityDecoder, HttpRoutes}
import org.typelevel.log4cats.Logger
import tech.diadochi.core.{Post, PostContent}
import tech.diadochi.repo.algebra.{PostContents, Posts}
import tech.diadochi.server.logging.syntax.*
import tech.diadochi.server.responses.FailureResponse

import java.time.LocalDateTime
import java.util.UUID
import scala.collection.mutable

class PostContentRoutes[F[_]: Concurrent: Logger] private (postContents: PostContents[F])
    extends Http4sDsl[F] {

  private val insertContentRoute: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root =>
    for {
      postInfo <- req.as[PostContent]
      post     <- postContents.create(postInfo)
      response <- Created(post)
    } yield response
  }

  private val updateContentRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) / "info" =>
      for {
        postInfo <- req.as[PostContent]
        post     <- postContents.update(postInfo)
        response <- post match {
          case Some(p) => Ok(p)
          case None    => NotFound(FailureResponse(s"Post with id $id not found"))
        }
      } yield response
  }

  private val deleteContentRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / UUIDVar(id) =>
      for {
        post <- postContents.delete(id, "en")
        response <-
          if (post == 0) NotFound(FailureResponse(s"Post with id $id not found"))
          else NoContent()
      } yield response
  }

  val routes: HttpRoutes[F] = Router(
    "/content/" -> (insertContentRoute <+> updateContentRoutes <+> deleteContentRoutes)
  )

}

object PostContentRoutes {
  def apply[F[_]: Concurrent: Logger](posts: PostContents[F]): PostContentRoutes[F] =
    new PostContentRoutes[F](posts)
}
