package tech.diadochi.repo.live

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import tech.diadochi.repo.fixtures.PostFixture
import tech.diadochi.repo.live.DoobieSpec

class PostsSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with DoobieSpec
    with PostFixture {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override val initScript: String = "sql/posts.sql"

  "Posts algebra" - {
    "should return no post if the given UUID does not exist" in {
      transactor.use { xa =>
        val program = for {
          posts     <- LivePosts[IO](xa)
          retrieved <- posts.find(notFoundUuid)
        } yield retrieved
        program asserting (_ shouldBe None)
      }
    }
    "should retrieve a post by id" in {
      transactor.use { xa =>
        val program = for {
          posts     <- LivePosts[IO](xa)
          retrieved <- posts.find(newPost.id)
        } yield retrieved
        program asserting (_ shouldBe Some(newPost))
      }
    }
  }

}
