import sbt._
import Keys._

name := "joinus-devops-service"

organization := "com.softwaremill"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.13.10"

mainClass := Some("com.softwaremill.Main")

libraryDependencies ++= {
  val AkkaVersion         = "2.7.0"
  val AkkaHttpVersion     = "10.4.0"
  val Json4sVersion       = "3.6.12"
  val ScalaLoggingVersion = "3.9.5"
  val ScalatestVersion    = "3.2.9"

  Seq(
    "com.typesafe.akka"          %% "akka-actor"    % AkkaVersion,
    "com.typesafe.akka"          %% "akka-stream"   % AkkaVersion,
    "com.typesafe.akka"          %% "akka-http"     % AkkaHttpVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion,
    "org.json4s"                 %% "json4s-native" % Json4sVersion,
    "com.typesafe.akka"          %% "akka-slf4j"    % AkkaVersion,
    "org.scalatest"              %% "scalatest"     % ScalatestVersion % "test"
  )
}
