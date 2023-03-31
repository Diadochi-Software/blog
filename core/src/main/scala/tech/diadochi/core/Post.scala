package tech.diadochi.core

import cats.Applicative

import java.time.LocalDateTime
import java.util.UUID
import cats.syntax.all.*

case class Post(
    id: UUID,
    authorEmail: String,
    originalLanguage: String,
    createdAt: LocalDateTime,
    tags: List[String] = List.empty,
    active: Boolean = false,
    maybeImage: Option[String] = None
)

object Post {

  def apply[F[_]: Applicative](authorEmail: String, info: PostContent): F[Post] =
    Post(
      id = UUID.randomUUID(),
      authorEmail = authorEmail,
      originalLanguage = info.language,
      createdAt = LocalDateTime.now()
    ).pure[F]
    
    
}
