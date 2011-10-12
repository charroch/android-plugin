package android

import java.io.File
import xml.XML

class Manifest(manifest: scala.xml.Elem) {

  def addons: Seq[String] = {
    Nil
  }

  def version: (String, Int) = {
    ("versionname", 123)
  }

  def sdkVersions: (Int, Int, Int) = {
    (1, 2, 3)
  }

  def pkg: String = {
    ""
  }

  def upVersion(f: (String, Int) => (String, Int)) = {}
}

object Manifest {
  def apply(f: File) = new Manifest(XML.loadFile(f))
}