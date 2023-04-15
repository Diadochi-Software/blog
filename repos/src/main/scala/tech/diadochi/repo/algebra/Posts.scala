package tech.diadochi.repo.algebra

import cats.Monad
import cats.syntax.all.*
import tech.diadochi.core.posts.{Post, PostContent}
import tech.diadochi.repo.filters.PostFilter
import tech.diadochi.repo.pagination.Pagination

import java.util.UUID

trait Posts[F[_]: Monad] {

  protected def postsInfo: PostContents[F]

  protected def insertPost(post: Post): F[UUID]

  def create(authorEmail: String, postInfo: PostContent): F[UUID] =
    for {
      post   <- Post[F](authorEmail, postInfo)
      postId <- insertPost(post)
      _      <- postsInfo.create(postInfo.copy(postId = Some(postId)))
    } yield postId

  def all(pagination: Pagination): F[List[Post]]

  def all(filter: PostFilter, pagination: Pagination): F[List[Post]]

  def find(id: UUID): F[Option[Post]]

  protected def updatePost(post: Post): F[Int]

  def update(post: Post): F[Option[Post]] =
    for {
      updatedPosts <- updatePost(post)
      maybePost <- updatedPosts match {
        case 0 => None.pure[F]
        case _ => find(post.id)
      }
    } yield maybePost

  protected def deletePost(id: UUID): F[Int]

  def delete(id: UUID): F[Int] =
    for {
      contents <- postsInfo.all(id)
      deletedInfos <- contents.traverse { postInfo =>
        postsInfo.delete(postInfo.postId.get, postInfo.language)
      }
      deletedPost <- deletePost(id)
    } yield deletedInfos.sum + deletedPost

}
