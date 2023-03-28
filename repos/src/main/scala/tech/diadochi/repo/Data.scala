package tech.diadochi.repo

import cats.effect.kernel.{MonadCancelThrow, Sync}
import cats.effect.{Async, Resource}
import cats.syntax.functor.toFunctorOps
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import tech.diadochi.core.Post
import tech.diadochi.repo.algebra.Posts
import tech.diadochi.repo.live.LivePosts

final case class Data[F[_]] private (posts: Posts[F])

object Data {

  private def postgresResource[F[_]: Async]: Resource[F, HikariTransactor[F]] =
    for {
      context <- ExecutionContexts.fixedThreadPool[F](32)
      transactor <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver",
        "jdbc:postgresql:blog", // TODO: Move to config
        "docker",
        "docker",
        context
      )
    } yield transactor

  def apply[F[_]: Async]: Resource[F, Data[F]] =
    postgresResource[F].evalMap { transactor =>
      for {
        posts <- LivePosts[F](transactor)
      } yield new Data[F](posts)
    }

}
