organization := "org.scala-tools.sbt"

name := "sbt-android-plugin"

version := "0.6.0-SNAPSHOT"

scalacOptions += "-deprecation"

publishMavenStyle := true

publishTo := Some("Scala Tools Nexus" at
                  "http://nexus.scala-tools.org/content/repositories/releases/")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

libraryDependencies ++= Seq(
  "com.google.android.tools" % "ddmlib" % "r10",
  "net.sf.proguard" % "proguard" % "4.4"
)

sbtPlugin := true

seq(ScriptedPlugin.scriptedSettings: _*)
