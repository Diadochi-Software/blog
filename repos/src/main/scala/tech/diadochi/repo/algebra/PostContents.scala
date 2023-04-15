package tech.diadochi.repo.algebra

import cats.Monad
import cats.syntax.all.*
import doobie.implicits.toSqlInterpolator
import tech.diadochi.core.posts.PostContent

import java.util.UUID

trait PostContents[F[_]: Monad] {

  def create(postInfo: PostContent): F[(UUID, String)]

  protected def updateContent(postInfo: PostContent): F[Int]

  def update(content: PostContent): F[Option[PostContent]] =
    for {
      updatedContent <- updateContent(content)
      maybeContent <- updatedContent match {
        case 0 => None.pure[F]
        case _ => find(content.postId.get, content.language)
      }
    } yield maybeContent

  def delete(id: UUID, language: String): F[Int]

  def all(postId: UUID): F[List[PostContent]]

  def find(id: UUID, language: String): F[Option[PostContent]]

}
