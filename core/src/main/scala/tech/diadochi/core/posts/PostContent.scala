package tech.diadochi.core.posts

import java.util.UUID
import scala.annotation.unused

case class PostContent(
    postId: Option[UUID],
    language: String,
    title: String,
    description: String,
    content: String
) {
  @unused
  def isNew: Boolean = postId.isEmpty
}
