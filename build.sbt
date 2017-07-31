organization := "com.micronautics"

name := "ktableFun"

version := "0.1.0"

scalaVersion := "2.12.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-target:jvm-1.8",
  "-unchecked",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Xlint"
)

scalacOptions in (Compile, doc) ++= baseDirectory.map {
  (bd: File) => Seq[String](
     "-sourcepath", bd.getAbsolutePath,
     "-doc-source-url", "https://github.com/mslinn/{name.value}/tree/master€{FILE_PATH}.scala"
  )
}.value

javacOptions ++= Seq(
  "-Xlint:deprecation",
  "-Xlint:unchecked",
  "-source", "1.8",
  "-target", "1.8",
  "-g:vars"
)

resolvers ++= Seq(
)

val kVer = "0.11.0.0"
libraryDependencies ++= Seq(
  "ch.qos.logback"    %  "logback-classic" % "1.2.3",
  "org.apache.kafka"  %  "kafka-streams"   % kVer withSources(),
  "org.apache.kafka"  %  "kafka-clients"   % kVer withSources(),
  //
  "org.scalatest"     %% "scalatest"       % "3.0.3" % Test withSources(),
  "junit"             %  "junit"           % "4.12"  % Test
)

logLevel := Level.Warn

// Only show warnings and errors on the screen for compilations.
// This applies to both test:compile and compile and is Info by default
logLevel in compile := Level.Warn

// Level.INFO is needed to see detailed output when running tests
logLevel in test := Level.Info

// define the statements initially evaluated when entering 'console', 'console-quick', but not 'console-project'
initialCommands in console := """
                                |""".stripMargin

cancelable := true
