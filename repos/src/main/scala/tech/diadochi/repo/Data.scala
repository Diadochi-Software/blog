package tech.diadochi.repo

import cats.effect.kernel.{MonadCancelThrow, Sync}
import cats.effect.{Async, Resource}
import cats.implicits.*
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import org.typelevel.log4cats.Logger
import tech.diadochi.core.posts.Post
import tech.diadochi.repo.algebra.{PostContents, Posts}
import tech.diadochi.repo.config.PostgresConfig
import tech.diadochi.repo.live.{LivePostContents, LivePosts}

final case class Data[F[_]] private (posts: Posts[F], content: PostContents[F])

object Data {

  private def postgresResource[F[_]: Async](
      config: PostgresConfig
  ): Resource[F, HikariTransactor[F]] =
    for {
      context <- ExecutionContexts.fixedThreadPool[F](config.numberOfThreads)
      transactor <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver",
        config.url,
        config.user,
        config.password,
        context
      )
    } yield transactor

  def apply[F[_]: Async: Logger](config: PostgresConfig): Resource[F, Data[F]] =
    postgresResource[F](config).evalMap { transactor =>
      for {
        posts   <- LivePosts[F](transactor)
        content <- LivePostContents[F](transactor)
      } yield new Data[F](posts, content)
    }

}
