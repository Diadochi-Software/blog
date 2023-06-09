ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala3Version
ThisBuild / organization := "diadochi"

lazy val diadochi      = "tech.diadochi"
lazy val scala3Version = "3.2.1"

lazy val circeVersion               = "0.14.0"
lazy val catsEffectVersion          = "3.3.14"
lazy val http4sVersion              = "0.23.15"
lazy val doobieVersion              = "1.0.0-RC1"
lazy val pureConfigVersion          = "0.17.1"
lazy val log4catsVersion            = "2.4.0"
lazy val tsecVersion                = "0.4.0"
lazy val scalaTestVersion           = "3.2.12"
lazy val scalaTestCatsEffectVersion = "1.4.0"
lazy val testContainerVersion       = "1.17.6"
lazy val logbackVersion             = "1.4.6"
lazy val slf4jVersion               = "2.0.0"
lazy val javaMailVersion            = "1.6.2"

val commonDependencies = Seq(
  "org.typelevel"         %% "cats-effect"     % catsEffectVersion,
  "org.typelevel"         %% "log4cats-slf4j"  % log4catsVersion,
  "com.github.pureconfig" %% "pureconfig-core" % pureConfigVersion
)

val testingDependencies = Seq(
  "org.typelevel"     %% "cats-effect-testing-scalatest" % scalaTestCatsEffectVersion % Test,
  "org.scalatest"     %% "scalatest"                     % scalaTestVersion           % Test,
  "org.typelevel"     %% "log4cats-noop"                 % log4catsVersion            % Test,
  "org.tpolecat"      %% "doobie-scalatest"              % doobieVersion              % Test,
  "org.testcontainers" % "testcontainers"                % testContainerVersion       % Test,
  "org.testcontainers" % "postgresql"                    % testContainerVersion       % Test,
  "ch.qos.logback"     % "logback-classic"               % logbackVersion             % Test
)

lazy val root = (project in file("."))
  .settings(
    name := "blog",
    libraryDependencies ++= commonDependencies ++ testingDependencies,
//      Seq(
//      "org.http4s"            %% "http4s-dsl"          % http4sVersion,
//      "org.http4s"            %% "http4s-ember-server" % http4sVersion,
//      "org.http4s"            %% "http4s-circe"        % http4sVersion,
//      "io.circe"              %% "circe-generic"       % circeVersion,
//      "io.circe"              %% "circe-fs2"           % circeVersion,
//      "org.tpolecat"          %% "doobie-core"         % doobieVersion,
//      "org.tpolecat"          %% "doobie-hikari"       % doobieVersion,
//      "org.tpolecat"          %% "doobie-postgres"     % doobieVersion,
//      "org.tpolecat"          %% "doobie-scalatest"    % doobieVersion    % Test,
//      "com.github.pureconfig" %% "pureconfig-core"     % pureConfigVersion,
//      "org.typelevel"         %% "log4cats-slf4j"      % log4catsVersion,
//      "org.slf4j"              % "slf4j-simple"        % slf4jVersion,
//      "io.github.jmcardon"    %% "tsec-http4s"         % tsecVersion,
//      "com.sun.mail"           % "javax.mail"          % javaMailVersion,
//      "ch.qos.logback"     % "logback-classic"               % logbackVersion             % Test
//    )
    Compile / mainClass := Some("tech.diadochi.blog.Main")
  )
  .dependsOn(server)
  .aggregate(authentication, server, repos, core)

lazy val core =
  (project in file("core")).settings(name := "core", libraryDependencies ++= commonDependencies)

lazy val authentication = (project in file("authentication"))
  .settings(
    name := "authentication",
    libraryDependencies ++= commonDependencies ++ Seq(
      "io.github.jmcardon" %% "tsec-http4s" % tsecVersion
    )
  )
  .dependsOn(core, repos)

lazy val repos = (project in file("repos"))
  .settings(
    name := "repos",
    libraryDependencies ++= commonDependencies ++ Seq(
      "org.tpolecat" %% "doobie-core"     % doobieVersion,
      "org.tpolecat" %% "doobie-hikari"   % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion
    )
  )
  .dependsOn(core)

lazy val server = (project in file("server"))
  .settings(
    name := "server",
    libraryDependencies ++= commonDependencies ++ Seq(
      "org.http4s" %% "http4s-dsl"          % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-circe"        % http4sVersion,
      "org.slf4j"   % "slf4j-simple"        % slf4jVersion,
      "io.circe"   %% "circe-generic"       % circeVersion,
      "io.circe"   %% "circe-fs2"           % circeVersion
      //      "io.github.jmcardon"    %% "tsec-http4s"         % tsecVersion,
      //      "com.sun.mail"           % "javax.mail"          % javaMailVersion,
      //      "org.typelevel"         %% "log4cats-noop"       % log4catsVersion  % Test,
      //      "org.scalatest"         %% "scalatest"           % scalaTestVersion % Test,
      //      "org.typelevel"     %% "cats-effect-testing-scalatest" % scalaTestCatsEffectVersion % Test,
      //      "org.testcontainers" % "testcontainers"                % testContainerVersion       % Test,
      //      "org.testcontainers" % "postgresql"                    % testContainerVersion       % Test
    )
  )
  .dependsOn(repos, core, authentication)
