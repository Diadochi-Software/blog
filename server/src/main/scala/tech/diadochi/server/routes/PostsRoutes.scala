package tech.diadochi.server.routes

import cats.effect.*
import cats.implicits.*
import cats.syntax.semigroup.*
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{EntityDecoder, HttpRoutes}
import tech.diadochi.core.{Post, PostInfo}
import tech.diadochi.server.responses.FailureResponse

import java.util.UUID
import scala.collection.mutable

class PostsRoutes[F[_]: Concurrent] private extends Http4sDsl[F] {

  private val database = new mutable.HashMap[UUID, Post]()

  private val allPostsRoute: HttpRoutes[F] = HttpRoutes.of[F] { case POST -> Root =>
    Ok(database.values)
  }

  private val findPostRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / UUIDVar(id) =>
    database.get(id) match
      case Some(post) => Ok(post)
      case None       => NotFound(FailureResponse(s"Post with id $id not found"))
  }

  private def createPost(postInfo: PostInfo): F[Post] =
    Post(
      id = UUID.randomUUID(),
      date = System.currentTimeMillis(),
      authorEmail = "john@doe.com",
      language = "English",
      info = postInfo,
      active = true
    ).pure[F]

  private val createPostRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "create" =>
      for {
        postInfo <- req.as[PostInfo]
        post     <- createPost(postInfo)
        resp     <- Created(post.id)
      } yield resp
  }

  private val updatePostRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      database get id match
        case Some(post) =>
          for {
            postInfo <- req.as[PostInfo]
            _        <- database.put(id, post.copy(info = postInfo)).pure[F]
            res      <- Ok()
          } yield res
        case None => NotFound(FailureResponse(s"Post with id $id not found"))
  }

  private val deletePostRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / UUIDVar(id) =>
      database get id match
        case Some(_) =>
          for {
            _   <- database.remove(id).pure[F]
            res <- Ok()
          } yield res
        case None => NotFound(FailureResponse(s"Post with id $id not found"))
  }

  val routes: HttpRoutes[F] = Router(
    "/posts" -> (allPostsRoute <+> findPostRoute <+> createPostRoute <+> updatePostRoute <+> deletePostRoute)
  )

}

object PostsRoutes {
  def apply[F[_]: Concurrent]: PostsRoutes[F] = new PostsRoutes[F]
}
