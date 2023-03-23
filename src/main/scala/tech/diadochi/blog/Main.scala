package tech.diadochi.blog

import cats.effect.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import tech.diadochi.server.HttpServer

object Main extends ResourceApp.Forever {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def run(args: List[String]): Resource[IO, Unit] =
    for {
      _ <- HttpServer[IO].start
    } yield ()

}
