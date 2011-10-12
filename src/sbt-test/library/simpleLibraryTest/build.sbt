import android._

organization := "org.scalatools.sbt"

name := "library"

version := "0.0.1"

seq(AndroidPlugin.librarySettings : _*)