package tech.diadochi.repo.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

final case class PostgresConfig(numberOfThreads: Int, url: String, user: String, password: String)
    derives ConfigReader
