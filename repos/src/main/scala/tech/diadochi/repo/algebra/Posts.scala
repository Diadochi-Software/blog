package tech.diadochi.repo.algebra

import tech.diadochi.core.{Post, PostInfo}

import java.util.UUID

trait Posts[F[_]] {

  def create(ownerEmail: String, postInfo: PostInfo): F[UUID]
  def all: F[List[Post]]
  def find(id: UUID): F[Option[Post]]
  def update(postInfo: Post): F[Option[Post]]
  def updateInfo(id: UUID, postInfo: PostInfo): F[Option[Post]]
  def delete(id: UUID): F[Int]

}
