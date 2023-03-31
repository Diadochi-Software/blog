package tech.diadochi.repo.live

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import tech.diadochi.repo.fixtures.PostFixture
import tech.diadochi.repo.live.DoobieSpec

class PostsSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with DoobieSpec
    with PostFixture {

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
  }

}
