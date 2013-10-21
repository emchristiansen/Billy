import sbt._
import Keys._

object BillyBuild extends Build {
  def extraResolvers = Seq(
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots"),
      //      "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
      //      "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
      //     "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/",
      //      "repo.codahale.com" at "http://repo.codahale.com",
      "Akka Repository" at "http://repo.akka.io/releases/",
      //      "spray-io" at "http://repo.spray.io/",
      "typesafe-releases" at "http://repo.typesafe.com/typesafe/repo",
      "Expecty Repository" at "https://raw.github.com/pniederw/expecty/master/m2repo/",
      "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository"))

  val projectName = "Billy"
  val mavenName = "billy"

  val publishSettings = Seq(
    name := mavenName,

    version := "0.1.1-SNAPSHOT",

    organization := "st.sparse",

    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (version.value.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },

    publishMavenStyle := true,

    publishArtifact in Test := false,

    pomIncludeRepository := { _ => false },

    licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),

    homepage := Some(url("https://github.com/emchristiansen/Billy")),

    pomExtra := (
      <scm>
        <url>git@github.com:emchristiansen/Billy.git</url>
        <connection>scm:git:git@github.com:emchristiansen/Billy.git</connection>
      </scm>
      <developers>
        <developer>
          <id>emchristiansen</id>
          <name>Eric Christiansen</name>
          <url>http://sparse.st</url>
        </developer>
      </developers>))      
      
  val scalaVersionString = "2.10.3"

  def extraLibraryDependencies = Seq(
    libraryDependencies ++= Seq(
      "opencv" % "opencv" % "3.0.0",
      "st.sparse" %% "sundry" % "0.1-SNAPSHOT",
      "st.sparse" %% "persistent-map" % "0.1.1-SNAPSHOT",
      //      "org.spark-project" %% "spark-core" % "0.7.0-SNAPSHOT",
      "org.scala-lang" %% "scala-pickling" % "0.8.0-SNAPSHOT",
      "com.sksamuel.scrimage" %% "scrimage-core" % "1.3.7",
      "com.sksamuel.scrimage" %% "scrimage-filters" % "1.3.7",
      //      "nebula" %% "nebula" % "0.1-SNAPSHOT",
//      "commons-lang" % "commons-lang" % "2.6",
      "org.scala-lang" % "scala-reflect" % scalaVersionString,
      "org.scala-lang" % "scala-compiler" % scalaVersionString,
//      "org.apache.commons" % "commons-math3" % "3.2",
      "commons-io" % "commons-io" % "2.4",
      "com.typesafe.slick" %% "slick" % "1.0.1",
      "org.scalatest" %% "scalatest" % "2.0.RC1-SNAP6" % "test",
      "org.expecty" % "expecty" % "0.9",
      //      "org.scalacheck" %% "scalacheck" % "1.10.1" % "test",
//      "org.scala-stm" %% "scala-stm" % "0.7",
//      "com.chuusai" %% "shapeless" % "1.2.4",
      "org.clapper" %% "grizzled-scala" % "1.1.4",
      "org.scalanlp" %% "breeze-math" % "0.4",
      "org.spire-math" %% "spire" % "0.6.0",
//      "org.scalaz" %% "scalaz-core" % "7.1.0-SNAPSHOT",
//      "org.rogach" %% "scallop" % "0.9.2",
      "junit" % "junit" % "4.11" % "test",
      "org.xerial" % "sqlite-jdbc" % "3.7.2",
      "org.slf4j" % "slf4j-simple" % "1.7.5" % "test",
//      "org.apache.logging.log4j" % "log4j-core" % "2.0-beta8",
      "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
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
      "-language:higherKinds",
      // "-language:reflectiveCalls",
      "-language:postfixOps",
      "-language:existentials",
      "-Xlint",
      //      "-Xlog-implicits",
      "-Yinline-warnings"))

  def libSettings =
    Project.defaultSettings ++
      extraResolvers ++
      extraLibraryDependencies ++
      scalaSettings ++
      updateOnDependencyChange ++
      publishSettings

  lazy val root = {
    val settings = libSettings ++ Seq(name := projectName, fork := true)
    Project(id = projectName, base = file("."), settings = settings)
  }
}
