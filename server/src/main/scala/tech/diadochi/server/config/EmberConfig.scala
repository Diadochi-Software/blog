package tech.diadochi.server.config

import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert
import pureconfig.generic.derivation.default.*

final case class EmberConfig(host: Host, port: Port) derives ConfigReader

object EmberConfig {

  given HostReader: ConfigReader[Host] = ConfigReader[String].emap { string =>
    Host
      .fromString(string)
      .toRight(CannotConvert(string, Host.getClass.toString, s"Invalid host: $string"))
  }

  given PortReader: ConfigReader[Port] = ConfigReader[Int].emap { int =>
    Port
      .fromInt(int)
      .toRight(CannotConvert(int.toString, Port.getClass.toString, s"Invalid port: $int"))
  }

}
