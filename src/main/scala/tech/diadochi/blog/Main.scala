package tech.diadochi.blog

import cats.effect.*
import tech.diadochi.server.HttpServer

object Main extends ResourceApp.Forever {

  def run(args: List[String]): Resource[IO, Unit] =
    for {
      _ <- HttpServer.start
    } yield ()

}
