package tech.diadochi.core

import java.time.LocalDateTime
import java.util.UUID

case class Post(
    id: UUID,
    authorEmail: String,
    originalLanguage: String,
    createdAt: LocalDateTime,
    tags: List[String] = List.empty,
    active: Boolean = false,
    maybeImage: Option[String] = None
)
