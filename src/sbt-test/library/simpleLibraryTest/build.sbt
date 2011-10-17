import _root_.android._
import AndroidKeys._

organization := "org.scalatools.sbt"

name := "library"

version := "0.0.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1" % "test"

seq(AndroidPlugin.librarySettings : _*)

seq(ProguardPlugin.proguardSettings :_*)

platformName := "android-13"

TaskKey[Unit]("check") <<= (target) map { (target) =>
  val f = SettingKey[String]("min-jar-path") ?? "???"
  println(f)
}

