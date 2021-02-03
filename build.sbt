name := "test_akka_rest"

version := "0.1"

scalaVersion := "2.12.13"

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.3"
val AkkaHttpCirceVersion = "1.31.0"

libraryDependencies ++= Seq(
  "de.heikoseeberger" %% "akka-http-circe" % AkkaHttpCirceVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
)

val circeVersion = "0.12.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
