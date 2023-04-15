package tech.diadochi.repo.live

import cats.effect.kernel.MonadCancelThrow
import cats.syntax.all.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.implicits.*
import doobie.util.*
import doobie.util.transactor.Transactor
import tech.diadochi.core.posts.{Post, PostContent}
import tech.diadochi.repo.algebra.{PostContents, Posts}

import java.util.UUID

private[repo] class LivePostContents[F[_]: MonadCancelThrow](xa: Transactor[F]) extends PostContents[F] {

  override def create(content: PostContent): F[(UUID, String)] =
    sql"""
      INSERT INTO post_info (post_id, lang, title, description, markdown)
      VALUES (${content.postId}, ${content.language}, ${content.title}, ${content.description}, ${content.content})
    """.update.withUniqueGeneratedKeys[(UUID, String)]("post_id", "lang").transact(xa)

  override def delete(id: UUID, language: String) =
    sql"""
      DELETE FROM post_info
      WHERE post_id = $id AND lang = $language
    """.update.run.transact(xa)

  override protected def updateContent(postInfo: PostContent): F[Int] =
    sql"""
        UPDATE post_info
        SET title = ${postInfo.title},
            description = ${postInfo.description},
            markdown = ${postInfo.content}
        WHERE post_id = ${postInfo.postId} AND language = ${postInfo.language}
      """.update.run.transact(xa)

  override def all(postId: UUID): F[List[PostContent]] =
    sql"""
      SELECT post_id, lang, title, description, markdown
      FROM post_info
      WHERE post_id = $postId
    """.query[PostContent].to[List].transact(xa)

  override def find(id: UUID, language: String): F[Option[PostContent]] =
    sql"""
      SELECT post_id, lang, title, description, markdown
      FROM post_info
      WHERE post_id = $id AND lang = $language
    """.query[PostContent].option.transact(xa)
}

object LivePostContents {

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): F[PostContents[F]] =
    new LivePostContents[F](xa).pure[F]

}
