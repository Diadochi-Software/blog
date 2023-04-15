package tech.diadochi.auth

import cats.effect.{IO, IOApp}
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

object Playground extends IOApp.Simple {

  override def run: IO[Unit] =
    BCrypt.hashpw[IO]("new password").flatMap(IO.println) *>
      BCrypt
        .checkpwBool[IO](
          "new password",
          PasswordHash[BCrypt]("$2a$10$Y3Sk4JuNGJCHtYv1WUfWlu/3iM90lprETis5MCeI4rYSZgEAFCYS.")
        )
        .flatMap(IO.println)

}
