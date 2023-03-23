package tech.diadochi.server.logging

import cats.*
import cats.implicits.*
import org.typelevel.log4cats.Logger

object syntax {

  extension [F[_], E, A](fa: F[A])(using M: MonadError[F, E], logger: Logger[F]) {

    def log(success: A => String, failure: E => String): F[A] =
      fa.attemptTap {
        case Left(e)  => logger.error(failure(e))
        case Right(a) => logger.info(success(a))
      }

    def logError(failure: E => String): F[A] =
      fa.attemptTap {
        case Left(e)  => logger.error(failure(e))
        case Right(_) => M.unit
      }

  }

}
