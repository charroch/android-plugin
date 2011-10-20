package android

import sbt._
import java.io.File
import Tools._

class Tools(aapt: File, manifest: File, res: Directory, assets: Directory, resourceApk: File, androidJar: File) {

  sealed trait Aapt
  case class Debug(debuggable:Boolean) extends Aapt



  private def generateApkResources(isLibrary:Boolean, isDebuggable: Boolean): File = {
    Process(<x>
      {aapt}
      package --auto-add-overlay -f
      -M
      {manifest}
      -S
      {res}
      -A
      {assets}
      -I
      {androidJar}
      -F
      {resourceApk}
    </x>).!

    resourceApk
  }
}

object Tools {
  type Directory = File
}