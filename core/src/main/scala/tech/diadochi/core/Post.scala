package tech.diadochi.core

import java.util.UUID

case class Post(
    id: UUID,
    date: Long,
    authorEmail: String,
    language: String,
    info: PostInfo,
    active: Boolean = false
)
