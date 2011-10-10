import android._

organization := "org.scala-tools.sbt"

name := "library"

version := "0.0.1"

seq(AndroidPlugin.librarySettings : _*)