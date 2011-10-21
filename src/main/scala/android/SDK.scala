package android

import android._

class sdk(root: Directory) {

  import sbt._

  lazy val aapt = root / "platform-tools" / "aapt"

  lazy val dx = root / "platform-tools" / "dx"

  lazy val aidl = root / "platform-tools" / "aapt"

  lazy val jars: Map[String, ModuleID] = {
    Map()
  }

  lazy val addons: Map[String, ModuleID] = {
    Map()
  }
}