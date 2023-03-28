package tech.diadochi.repo.live

import cats.effect.kernel.MonadCancelThrow
import cats.syntax.all.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.implicits.*
import doobie.util.*
import doobie.util.transactor.Transactor
import tech.diadochi.core.{Post, PostInfo}
import tech.diadochi.repo.algebra.Posts

import java.time.LocalDateTime
import java.util.UUID

class LivePosts[F[_]: MonadCancelThrow] private (xa: Transactor[F]) extends Posts[F] {

  private def createPost(authorEmail: String, info: PostInfo) =
    Post(
      id = UUID.randomUUID(),
      authorEmail = authorEmail,
      originalLanguage = info.language,
      createdAt = LocalDateTime.now()
    ).pure[F]

  private def insertPost(post: Post): F[UUID] =
    sql"""
      INSERT INTO posts (id, author_email, original_language, created_at, tags, isActive, image)
      VALUES (${post.id}, ${post.authorEmail}, ${post.originalLanguage}, ${post.createdAt}, ${post.tags}, ${post.active}, ${post.maybeImage})
    """.update.withUniqueGeneratedKeys[UUID]("id").transact(xa)

  private def insertPostInfo(postId: UUID, info: PostInfo) =
    sql"""
      INSERT INTO post_info (post_id, lang, title, description, markdown)
      VALUES ($postId, ${info.language}, ${info.title}, ${info.description}, ${info.content})
    """.update.run.transact(xa)

  override def create(authorEmail: String, postInfo: PostInfo): F[UUID] =
    for {
      post   <- createPost(authorEmail, postInfo)
      postId <- insertPost(post)
      _      <- insertPostInfo(postId, postInfo)
    } yield postId

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

  override def update(post: Post): F[Option[Post]] =
    sql"""
      UPDATE posts
      SET author_email = ${post.authorEmail},
          original_language = ${post.originalLanguage},
          created_at = ${post.createdAt},
          tags = ${post.tags},
          isActive = ${post.active},
          image = ${post.maybeImage.orNull}
      WHERE id = ${post.id}
    """.update.run.transact(xa).flatMap {
      case 0 => None.pure[F]
      case _ => find(post.id)
    }

  override def updateInfo(id: UUID, postInfo: PostInfo): F[Option[Post]] =
    sql"""
      UPDATE post_info
      SET lang = ${postInfo.language},
          title = ${postInfo.title},
          description = ${postInfo.description},
          markdown = ${postInfo.content}
      WHERE post_id = $id
    """.update.run.transact(xa).flatMap {
      case 0 => None.pure[F]
      case _ => find(id)
    }

  private def deletePost(id: UUID) =
    sql"""
      DELETE FROM posts
      WHERE id = $id
    """.update.run.transact(xa)

  private def deletePostInfo(id: UUID) =
    sql"""
      DELETE FROM post_info
      WHERE post_id = $id
    """.update.run.transact(xa)

  override def delete(id: UUID): F[Int] =
    for {
      deletedInfo <- deletePostInfo(id)
      _ <-
        if deletedInfo == 0 then 0.pure[F]
        else deletePost(id)
    } yield deletedInfo + 1

}

object LivePosts {

  given postRead: Read[Post] = Read[
    (
        UUID,
        String,
        String,
        LocalDateTime,
        List[String],
        Boolean,
        Option[String]
    )
  ].map {
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
    new LivePosts[F](xa).pure[F]

}
