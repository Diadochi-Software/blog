package tech.diadochi.blog.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*
import tech.diadochi.repo.config.PostgresConfig
import tech.diadochi.server.config.EmberConfig

final case class AppConfig(db: PostgresConfig, server: EmberConfig) derives ConfigReader
