import sbt._
import Keys._

val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.12"
val logBackClassic = "ch.qos.logback" % "logback-classic" % "1.1.3"
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
val json4s = "org.json4s" %% "json4s-native" % "3.2.10"
val akkaHttp = "com.typesafe.akka" %% "akka-http-experimental" % "1.0"
val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % "2.3.12"
val scalatest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"

name := "joinus-devops-service"

organization := "com.softwaremill"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= List(slf4jApi, logBackClassic, scalaLogging, json4s, akkaHttp, akkaSlf4j, scalatest)