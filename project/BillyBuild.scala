import sbt._
import Keys._

import sbtassembly.Plugin._
import AssemblyKeys._

object BillyBuild extends Build {
  def extraResolvers = Seq(
    resolvers ++= Seq(
      "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
      "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
      //     "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/",
      //      "repo.codahale.com" at "http://repo.codahale.com",
      "Akka Repository" at "http://repo.akka.io/releases/",
      "spray-io" at "http://repo.spray.io/",
      "typesafe-releases" at "http://repo.typesafe.com/typesafe/repo",
      "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository"))

  val scalaVersionString = "2.10.2"

  def extraLibraryDependencies = Seq(
    libraryDependencies ++= Seq(
      "opencv" % "opencv" % "2.4.9",
      //      "org.spark-project" %% "spark-core" % "0.7.0-SNAPSHOT",
      "org.scala-lang" %% "scala-pickling" % "0.8.0-SNAPSHOT",
      "com.sksamuel.scrimage" % "scrimage-core" % "1.3.0",
      "com.sksamuel.scrimage" % "scrimage-filters" % "1.3.0",
      "nebula" %% "nebula" % "0.1-SNAPSHOT",
      "commons-lang" % "commons-lang" % "2.6",
      "org.scala-lang" % "scala-reflect" % scalaVersionString,
      "org.scala-lang" % "scala-compiler" % scalaVersionString,
      "org.apache.commons" % "commons-math3" % "3.2",
      "commons-io" % "commons-io" % "2.4",
      "org.scalatest" %% "scalatest" % "2.0.M6-SNAP9" % "test",
      "org.scalacheck" %% "scalacheck" % "1.10.1" % "test",
      "org.scala-stm" %% "scala-stm" % "0.7",
      "com.chuusai" %% "shapeless" % "1.2.4",
      "org.clapper" %% "grizzled-scala" % "1.1.4",
      "org.scalanlp" %% "breeze-math" % "0.4-SNAPSHOT",
      "org.spire-math" %% "spire" % "0.6.0-M1",
      "org.scalaz" %% "scalaz-core" % "7.1.0-SNAPSHOT",
      "io.spray" %% "spray-json" % "1.2.3",
      "org.rogach" %% "scallop" % "0.9.2",
      "junit" % "junit" % "4.11" % "test",
      "org.imgscalr" % "imgscalr-lib" % "4.2"))

  def updateOnDependencyChange = Seq(
    watchSources <++= (managedClasspath in Test) map { cp => cp.files })

  def scalaSettings = Seq(
    scalaVersion := scalaVersionString,
    scalacOptions ++= Seq(
      "-optimize",
      "-unchecked",
      "-deprecation",
      "-feature",
      "-language:implicitConversions",
      // "-language:reflectiveCalls",
      "-language:postfixOps",
      "-Yinline-warnings"))

  def libSettings =
    Project.defaultSettings ++
      extraResolvers ++
      extraLibraryDependencies ++
      scalaSettings ++
      assemblySettings ++
      updateOnDependencyChange

  val projectName = "Billy"
  lazy val root = {
    val settings = libSettings ++ Seq(name := projectName, fork := true)
    Project(id = projectName, base = file("."), settings = settings)
  }
}