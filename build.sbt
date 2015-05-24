name := "heist"

scalaVersion := "2.11.6"

libraryDependencies ++= google ++ scalaz ++ argonaut

lazy val google = Seq(
  "com.google.apis" % "google-api-services-drive" % "v2-rev170-1.20.0"
)

lazy val argonaut = Seq("io.argonaut" %% "argonaut" % "6.1")

lazy val scalaz = Seq("org.scalaz" %% "scalaz-core" % "7.1.2")