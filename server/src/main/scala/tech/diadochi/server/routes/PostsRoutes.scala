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
import tech.diadochi.repo.algebra.Posts
import tech.diadochi.server.logging.syntax.*
import tech.diadochi.server.responses.FailureResponse

import java.time.LocalDateTime
import java.util.UUID
import scala.collection.mutable

class PostsRoutes[F[_]: Concurrent: Logger] private (posts: Posts[F]) extends Http4sDsl[F] {

  private val allPostsRoute: HttpRoutes[F] = HttpRoutes.of[F] { case POST -> Root =>
    for {
      posts    <- posts.all
      response <- Ok(posts)
    } yield response
  }

  private val findPostRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / UUIDVar(id) =>
    for {
      maybePost <- posts.find(id)
      response <- maybePost match {
        case Some(post) => Ok(post)
        case None       => NotFound(FailureResponse(s"Post with id $id not found"))
      }
    } yield response
  }

  private val createPostRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "create" =>
      (for {
        postInfo <- req.as[PostContent]
        post     <- posts.create("john@doe.com", postInfo)
        response <- Created(post)
      } yield response).logError(_ => "Failed to create post")
  }

  private val updatePostRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      for {
        post <- req.as[Post]
        post <- posts.update(post)
        response <- post match {
          case Some(p) => Ok(p)
          case None    => NotFound(FailureResponse(s"Post with id $id not found"))
        }
      } yield response
  }

//  private val updatePostInfoRoute: HttpRoutes[F] = HttpRoutes.of[F] {
//    case req @ PUT -> Root / UUIDVar(id) / "info" =>
//      for {
//        postInfo <- req.as[PostContent]
//        post     <- posts.updateInfo(id, postInfo)
//        response <- post match {
//          case Some(p) => Ok(p)
//          case None    => NotFound(FailureResponse(s"Post with id $id not found"))
//        }
//      } yield response
//  }

  private val deletePostRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / UUIDVar(id) =>
      for {
        post <- posts.delete(id)
        response <-
          if (post == 0) NotFound(FailureResponse(s"Post with id $id not found"))
          else Ok(post)
      } yield response
  }

  val routes: HttpRoutes[F] = Router(
    "/posts" -> (allPostsRoute <+> findPostRoute <+> createPostRoute <+> updatePostRoute <+> deletePostRoute)
  )

}

object PostsRoutes {
  def apply[F[_]: Concurrent: Logger](posts: Posts[F]): PostsRoutes[F] = new PostsRoutes[F](posts)
}
