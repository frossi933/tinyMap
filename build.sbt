val Http4sVersion        = "0.22.7"
val CirceVersion         = "0.13.0"
val LogbackVersion       = "1.2.5"
val DoobieVersion        = "0.13.4"
val KindProjectorVersion = "0.13.0"
val NewTypeVersion       = "0.4.4"
val RefinedVersion       = "0.9.28"

lazy val root = (project in file("."))
  .settings(
    organization := "coding.tiny",
    name         := "TinyMap",
    version      := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"    %% "http4s-circe"        % Http4sVersion,
      "org.http4s"    %% "http4s-dsl"          % Http4sVersion,
      "io.circe"      %% "circe-generic"       % CirceVersion,
      "io.circe"      %% "circe-core"          % CirceVersion,
      "io.circe"      %% "circe-refined"       % CirceVersion,
      "ch.qos.logback" % "logback-classic"     % LogbackVersion,
      "org.tpolecat"  %% "doobie-core"         % DoobieVersion,
      "org.tpolecat"  %% "doobie-postgres"     % DoobieVersion,
      "org.tpolecat"  %% "doobie-refined"      % DoobieVersion,
      "io.estatico"   %% "newtype"             % NewTypeVersion,
      "eu.timepit"    %% "refined"             % RefinedVersion
    ),
    addCompilerPlugin(
      "org.typelevel" %% "kind-projector" % KindProjectorVersion cross CrossVersion.full
    ),
    scalacOptions += "-Ymacro-annotations"
  )
