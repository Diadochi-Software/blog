package tech.diadochi.core

case class PostInfo(
    author: String,
    title: String,
    description: String,
    content: String,
    tags: List[String],
    maybeImage: Option[String] = None
)

object PostInfo {

  def empty: PostInfo = PostInfo("", "", "", "", List.empty)

}
