package tech.diadochi.repo.live

import cats.effect.kernel.MonadCancelThrow
import cats.syntax.all.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.implicits.*
import doobie.util.*
import doobie.util.transactor.Transactor
import doobie.{Fragments, Meta}
import org.typelevel.log4cats.Logger
import tech.diadochi.core.users.{Role, User}
import tech.diadochi.repo.algebra.Users

private[repo] class LiveUsers[F[_]: MonadCancelThrow: Logger] private (xa: Transactor[F])
    extends Users[F] {

  import LiveUsers.given

  override def find(email: String): F[Option[User]] =
    sql"SELECT * FROM users WHERE email = $email"
      .query[User]
      .option
      .transact(xa)

  override def create(user: User): F[String] =
    sql"""INSERT INTO users(email, hashedPassword, firstName, lastName, company, role)
    VALUES(${user.email}, ${user.hashedPassword}, ${user.firstName}, ${user.lastName}, ${user.company}, ${user.role});""".update.run
      .transact(xa) *> user.email.pure[F]

  override def update(user: User): F[Option[User]] =
    sql"""UPDATE users SET
            hashedPassword = ${user.hashedPassword},
            firstName = ${user.firstName},
            lastName = ${user.lastName},
            company = ${user.company},
            role = ${user.role}
          WHERE email = ${user.email}""".update.run
      .transact(xa) *> find(user.email)

  override def delete(email: String): F[Boolean] =
    sql"DELETE FROM users WHERE email = $email".update.run.transact(xa).map(_ > 0)

}

object LiveUsers {

  given metaRole: Meta[Role] = Meta[String].timap(Role.valueOf)(_.toString)

  def apply[F[_]: MonadCancelThrow: Logger](xa: Transactor[F]): F[LiveUsers[F]] =
    new LiveUsers[F](xa).pure[F]

}
