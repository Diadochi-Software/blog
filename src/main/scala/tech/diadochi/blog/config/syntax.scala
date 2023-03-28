package tech.diadochi.blog.config

import cats.MonadThrow
import cats.syntax.all.*
import pureconfig.error.ConfigReaderException
import pureconfig.{ConfigReader, ConfigSource}

import scala.annotation.unused
import scala.reflect.ClassTag

object syntax {

  extension (source: ConfigSource) {
    def loadF[F[_], A](using reader: ConfigReader[A], F: MonadThrow[F], @unused tag: ClassTag[A]): F[A] =
      F.pure(source.load[A]).flatMap {
        case Right(value) => F.pure(value)
        case Left(error)  => F.raiseError[A](ConfigReaderException(error))
      }
  }

}
