package tech.diadochi.server.routes

import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits.*
import cats.syntax.semigroup.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.*
import org.http4s.implicits.*
import org.http4s.server.Router
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import tech.diadochi.core.{Post, PostContent}
import tech.diadochi.repo.algebra.{PostContents, Posts}
import tech.diadochi.repo.filters.PostFilter
import tech.diadochi.repo.pagination.Pagination
import tech.diadochi.server.fixtures.PostFixture
import tech.diadochi.server.logging.syntax.*
import tech.diadochi.server.responses.FailureResponse

import java.util.UUID

class PostsRoutesSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with Http4sDsl[IO]
    with PostFixture {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val postContents: PostContents[IO] = new PostContents[IO] {

    override def create(postInfo: PostContent): IO[(UUID, String)] = IO.pure((newPostUuid, "en"))

    override protected def updateContent(postInfo: PostContent): IO[Int] =
      if (postInfo.postId == newPostUuid) IO.pure(1)
      else IO.pure(0)

    override def delete(id: UUID, language: String): IO[Int] =
      if (id == newPostUuid) IO.pure(1)
      else IO.pure(0)

    override def all(postId: UUID): IO[List[PostContent]] =
      if (postId == newPostUuid) IO.pure(List(newPostInfo))
      else IO.pure(List.empty)

    override def find(id: UUID, language: String): IO[Option[PostContent]] =
      if (id == newPostUuid && language == "en") IO.pure(Some(newPostInfo))
      else IO.pure(None)

  }

  val posts: Posts[IO] = new Posts[IO] {

    override protected def postsInfo: PostContents[IO] = postContents

    override protected def insertPost(post: Post): IO[UUID] = IO.pure(newPostUuid)

    override def all: IO[List[Post]] = IO.pure(List(newPost))

    override def find(id: UUID): IO[Option[Post]] =
      if (id == newPostUuid) IO.pure(Some(newPost))
      else IO.pure(None)

    override protected def updatePost(post: Post): IO[Int] =
      if (post.id == newPostUuid) IO.pure(1)
      else IO.pure(0)

    override def update(post: Post): IO[Option[Post]] =
      if (post.id == newPostUuid) IO.pure(Some(updatedPost))
      else IO.pure(None)

    override protected def deletePost(id: UUID): IO[Int] =
      if (id == newPostUuid) IO.pure(1)
      else IO.pure(0)

    override def all(filter: PostFilter, pagination: Pagination): IO[List[Post]] =
      IO.pure(List(newPost))
  }

  private val postRoutes: HttpRoutes[IO] = PostsRoutes[IO](posts).routes

  "PostsRoutes" - {
    "should return a post with a given id" in {
      for {
        response <- postRoutes.orNotFound.run(
          Request[IO](
            method = Method.GET,
            uri"/posts/00000000-0000-0000-0000-000000000001"
          )
        )
        body <- response.as[Post]
      } yield {
        response.status shouldBe Ok
        body shouldBe newPost
      }
    }
    "should return all posts" in {
      for {
        response <- postRoutes.orNotFound.run(
          Request[IO](
            method = Method.POST,
            uri"/posts"
          )
        )
        body <- response.as[List[Post]]
      } yield {
        response.status shouldBe Ok
        body shouldBe List(newPost)
      }
    }
    "should create a new post" in {
      for {
        response <- postRoutes.orNotFound.run(
          Request[IO](
            method = Method.POST,
            uri"/posts/create"
          ).withEntity(newPostInfo)
        )
        body <- response.as[UUID]
      } yield {
        response.status shouldBe Created
        body shouldBe newPostUuid
      }
    }
    "should update a post" in {
      for {
        response <- postRoutes.orNotFound.run(
          Request[IO](
            method = Method.PUT,
            uri"/posts/00000000-0000-0000-0000-000000000001"
          ).withEntity(updatedPost)
        )
        body <- response.as[Post]
      } yield {
        response.status shouldBe Ok
        body shouldBe updatedPost
      }
    }
    "should delete a post" in {
      for {
        response <- postRoutes.orNotFound.run(
          Request[IO](
            method = Method.DELETE,
            uri"/posts/00000000-0000-0000-0000-000000000001"
          )
        )
      } yield response.status shouldBe NoContent
    }
    "should only delete a post that exists" in {
      for {
        response <- postRoutes.orNotFound.run(
          Request[IO](
            method = Method.DELETE,
            uri"/posts/00000000-0000-0000-0000-000000000002"
          )
        )
      } yield response.status shouldBe NotFound
    }
    "should return 404 if a post is not found" in {
      for {
        response <- postRoutes.orNotFound.run(
          Request[IO](
            method = Method.GET,
            uri"/posts/00000000-0000-0000-0000-000000000002"
          )
        )
        body <- response.as[FailureResponse]
      } yield {
        response.status shouldBe NotFound
        body shouldBe FailureResponse("Post with id 00000000-0000-0000-0000-000000000002 not found")
      }
    }
  }

}
