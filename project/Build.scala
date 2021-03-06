import sbt._
import sbt.Keys._

object BuildSettings {

  val Name = "prognos-scalding-example"
  val Version = "1.0.0"
  val ScalaVersion = "2.10.5"

  import Scalding._

  val basicSettings = Defaults.defaultSettings ++ scaldingSettings ++ Seq (
    name          := Name,
    version       := Version,
    scalaVersion  := ScalaVersion,
    organization  := "com.learn",
    description   := "Mapreduce with scalding book source",
    scalacOptions := Seq("-deprecation", "-unchecked", "-encoding", "utf8")
  )

  // sbt-assembly settings for building a fat jar that includes all dependencies.
  // This is useful for running Hadoop jobs, but not needed for local script testing.
  // Adapted from https://github.com/snowplow/scalding-example-project
  import sbtassembly.Plugin._
  import AssemblyKeys._
  lazy val sbtAssemblySettings = assemblySettings ++ Seq(

    // Slightly cleaner jar name
    jarName in assembly := s"${name.value}-${version.value}.jar"  ,

    // Drop these jars, most of which are dependencies of dependencies and already exist
    // in Hadoop deployments or aren't needed for local mode execution. Some are older
    // versions of jars that collide with newer versions in the dependency graph!!
    excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
      val excludes = Set(
        "scala-compiler.jar",
        "jsp-api-2.1-6.1.14.jar",
        "jsp-2.1-6.1.14.jar",
        "jasper-compiler-5.5.12.jar",
        "minlog-1.2.jar", // Otherwise causes conflicts with Kyro (which Scalding pulls in)
        "janino-2.5.16.jar", // Janino includes a broken signature, and is not needed anyway
        "commons-beanutils-core-1.8.0.jar", // Clash with each other and with commons-collections
        "commons-beanutils-1.7.0.jar",
        "stax-api-1.0.1.jar",
        "asm-3.1.jar",
        "scalatest-2.0.jar"
      )
      cp filter { jar => excludes(jar.data.getName) }
    },

    mergeStrategy in assembly <<= (mergeStrategy in assembly) {
      (old) => {
        case "project.clj" => MergeStrategy.discard // Leiningen build files
        case PathList("org","apache","hadoop","package-info.class") => MergeStrategy.first
        case PathList("org","hamcrest", xs @ _*) => MergeStrategy.first
        case PathList("org","objenesis", xs @ _*) => MergeStrategy.first
        case x => old(x)
      }
    }
  )

  lazy val buildSettings = basicSettings ++ sbtAssemblySettings
}


object Resolvers {
  val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  val sonatype = "Sonatype Release" at "https://oss.sonatype.org/content/repositories/releases"
  val mvnrepository = "MVN Repo" at "http://mvnrepository.com/artifact"
  val conjars  = "Concurrent Maven Repo" at "http://conjars.org/repo"
  val clojars  = "Clojars Repo" at "http://clojars.org/repo"
  val twitterMaven = "Twitter Maven" at "http://maven.twttr.com"

  val allResolvers = Seq(typesafe, sonatype, mvnrepository, conjars, clojars, twitterMaven)

}

object Dependency {
  object Version {
    val Scalding  = "0.13.1"//"0.10.0"
    val Algebird  = "0.9.0"
    val Hadoop    = "2.0.0-mr1-cdh4.5.0"
    val HadoopCDH    = "2.0.0-cdh4.5.0"
    val ScalaTest = "2.0"
  }

  // ---- Application dependencies ----

  // Include the Scala compiler itself for reification and evaluation of expressions.
  val scalaCompiler  = "org.scala-lang" %  "scala-compiler" % BuildSettings.ScalaVersion

  val scalding_args  = "com.twitter"    %% "scalding-args"  % Version.Scalding
  val scalding_core  = "com.twitter"    %% "scalding-core"  % Version.Scalding
  val scalding_date  = "com.twitter"    %% "scalding-date"  % Version.Scalding
  val algebird_core  = "com.twitter"    %% "algebird-core"  % Version.Algebird
  val algebird_util  = "com.twitter"    %% "algebird-util"  % Version.Algebird

  val hadoop_core    = "org.apache.hadoop" % "hadoop-core"  % Version.Hadoop
  val hadoop_common  = "org.apache.hadoop" % "hadoop-common"  % Version.HadoopCDH

  val prognos = "com.fk" % "prognos_2.10" % "1.0"

  val scalaTest      = "org.scalatest"     % "scalatest_2.10" % Version.ScalaTest %  "test"
}

object Dependencies {
  import Dependency._

  val activatorscalding = Seq(
    scalaCompiler, scalding_args, scalding_core, scalding_date,
    algebird_core, algebird_util, hadoop_core, hadoop_common, prognos, scalaTest)
}

object PrognosScaldingBuild extends Build {
  import Resolvers._
  import Dependencies._
  import BuildSettings._

  lazy val project = Project(
    id = "prognos-scalding-example",
    base = file("."),
    settings = buildSettings ++ Seq(
      // runScriptSetting,
      resolvers := allResolvers,
      libraryDependencies ++= Dependencies.activatorscalding,
      mainClass := Some("Run")))
}
