package tech.diadochi.repo.live

import cats.effect.kernel.MonadCancelThrow
import cats.syntax.all.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.implicits.*
import doobie.util.*
import doobie.util.transactor.Transactor
import tech.diadochi.core.{Post, PostContent}
import tech.diadochi.repo.algebra.{PostContents, Posts}

import java.time.LocalDateTime
import java.util.UUID

class LivePosts[F[_]: MonadCancelThrow] private (
    xa: Transactor[F],
    override val postsInfo: PostContents[F]
) extends Posts[F] {

  override protected def insertPost(post: Post): F[UUID] =
    sql"""
      INSERT INTO posts (id, author_email, original_language, created_at, tags, isActive, image)
      VALUES (${post.id}, ${post.authorEmail}, ${post.originalLanguage}, ${post.createdAt}, ${post.tags}, ${post.active}, ${post.maybeImage})
    """.update.withUniqueGeneratedKeys[UUID]("id").transact(xa)

  override def all: F[List[Post]] =
    sql"""
      SELECT id, author_email, original_language, created_at, tags, isActive, image
      FROM posts
    """.query[Post].to[List].transact(xa)

  override def find(id: UUID): F[Option[Post]] =
    sql"""
      SELECT id, author_email, original_language, created_at, tags, isActive, image
      FROM posts
      WHERE id = $id
    """.query[Post].option.transact(xa)

  override protected def updatePost(post: Post): F[Int] =
    sql"""
      UPDATE posts
      SET author_email = ${post.authorEmail},
          original_language = ${post.originalLanguage},
          created_at = ${post.createdAt},
          tags = ${post.tags},
          isActive = ${post.active},
          image = ${post.maybeImage.orNull}
      WHERE id = ${post.id}
    """.update.run.transact(xa)

  override protected def deletePost(id: UUID): F[Int] =
    sql"""
      DELETE FROM posts
      WHERE id = $id
    """.update.run.transact(xa)

}

object LivePosts {

  type PostData = (
      UUID,
      String,
      String,
      LocalDateTime,
      List[String],
      Boolean,
      Option[String]
  )

  given postRead: Read[Post] = Read[PostData].map {
    case (
          id,
          authorEmail,
          originalLanguage,
          date,
          tags,
          active,
          maybeImage
        ) =>
      Post(
        id,
        authorEmail,
        originalLanguage,
        date,
        tags,
        active,
        maybeImage
      )
  }

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): F[LivePosts[F]] =
    for {
      postsInfo <- LivePostContents[F](xa)
    } yield new LivePosts[F](xa, postsInfo)

}
