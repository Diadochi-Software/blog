package tech.diadochi.repo.fixtures

import tech.diadochi.core.{Post, PostContent}

import java.time.LocalDateTime
import java.util.UUID

trait PostFixture {

  protected val newPostUuid: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")

  protected val newPost: Post =
    Post(
      newPostUuid,
      "john@doe.com",
      "en",
      LocalDateTime.parse("2023-03-31T18:56:32.728924"),
      List("tag1", "tag2")
    )

  protected val updatedPost: Post =
    Post(newPostUuid, "jane@doe.com", "es", LocalDateTime.parse("2023-03-31T18:56:32.728924"))

  protected val newPostInfo: PostContent =
    PostContent(Some(newPostUuid), "en", "Awesome Post", "An awesome post", "Awesome content")

  protected val updatedPostInfo: PostContent =
    PostContent(
      Some(newPostUuid),
      "es",
      "Post Increíble",
      "Un post increíble",
      "Contenido increíble"
    )

  protected val notFoundUuid: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")

}
