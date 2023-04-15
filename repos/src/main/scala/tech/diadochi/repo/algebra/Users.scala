package tech.diadochi.repo.algebra

import tech.diadochi.core.users.User

trait Users[F[_]] {

  def find(email: String): F[Option[User]]
  def create(user: User): F[String]
  def update(user: User): F[Option[User]]
  def delete(email: String): F[Boolean]

}
