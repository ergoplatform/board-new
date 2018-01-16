organization := "org.ergoplatform"

name := "ergo-board"

version := "0.0.1"

scalaVersion := "2.12.4"

resolvers += Resolver.bintrayRepo("hseeberger", "maven")
resolvers += Resolver.sonatypeRepo("releases")

val akkaHttpV = "10.0.11"
val akkaV = "2.5.7"
val reactiveMongoV = "0.12.6"
val reactiveMongoPlayJsonV = reactiveMongoV + "-play26"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpV,
  "com.typesafe.akka" %% "akka-persistence" % akkaV,
  "com.typesafe.play" %% "play-json" % "2.6.7",
  "de.heikoseeberger" %% "akka-http-play-json" % "1.18.1",
  "org.reactivemongo" %% "reactivemongo-play-json" % reactiveMongoPlayJsonV,
  "org.reactivemongo" %% "reactivemongo" % reactiveMongoV,
  "org.reactivemongo" %% "reactivemongo-akkastream" % reactiveMongoV,
  "com.github.scullxbones" %% "akka-persistence-mongo-rxmongo" % "2.0.4",
  "org.scorexfoundation" %% "scrypto" % "2.0.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test,
  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.0.0" % Test
)

parallelExecution in Test := false

scalacOptions := Seq("-feature")
