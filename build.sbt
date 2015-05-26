name := "heist"

scalaVersion := "2.11.6"

libraryDependencies ++= algebra ++ cats ++ google

lazy val google = Seq(
  "com.google.apis"  % "google-api-services-drive" % "v2-rev170-1.20.0",
  "com.google.gdata" % "core"                      % "1.47.1"
)

val catsVersion = "0.1.0-SNAPSHOT"

lazy val cats = Seq(
  "org.spire-math" %% "cats-core"    % catsVersion,
  "org.spire-math" %% "cats-std"     % catsVersion,
  "org.spire-math" %% "cats-state"   % catsVersion,
  "org.spire-math" %% "cats-free"    % catsVersion,
  "org.spire-math" %% "cats-laws"    % catsVersion % Test
)

lazy val algebra = Seq(
  "org.spire-math" %% "algebra" % "0.2.0-SNAPSHOT" from "http://plastic-idolatry.com/jars/algebra_2.11-0.2.0-SNAPSHOT.jar",
  "org.spire-math" %% "algebra-std" % "0.2.0-SNAPSHOT" from "http://plastic-idolatry.com/jars/algebra-std_2.11-0.2.0-SNAPSHOT.jar",
  "org.spire-math" %% "algebra-laws" % "0.2.0-SNAPSHOT" % Test from "http://plastic-idolatry.com/jars/algebra-laws_2.11-0.2.0-SNAPSHOT.jar"
)

scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"
  )