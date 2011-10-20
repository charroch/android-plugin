package android

import sbt._

import Keys._
import AndroidKeys._
import AndroidKeys.{android => androidkey}

object AndroidDefaults {
  val DefaultAaaptName = "aapt"
  val DefaultAadbName = "adb"
  val DefaultAaidlName = "aidl"
  val DefaultDxName = "dx"
  val DefaultAndroidManifestName = "AndroidManifest.xml"
  val DefaultAndroidJarName = "android.jar"
  val DefaultMapsJarName = "maps.jar"
  val DefaultAssetsDirectoryName = "assets"
  val DefaultResDirectoryName = "res"
  val DefaultClassesMinJarName = "classes.min.jar"
  val DefaultClassesDexName = "classes.dex"
  val DefaultResourcesApkName = "resources.apk"
  val DefaultDxJavaOpts = "-JXmx512m"
  val DefaultManifestSchema = "http://schemas.android.com/apk/res/android"
  val DefaultEnvs = List("ANDROID_SDK_HOME", "ANDROID_SDK_ROOT", "ANDROID_HOME")

  lazy val settings: Seq[Setting[_]] = Seq(
    aaptName := DefaultAaaptName,
    adbName := DefaultAadbName,
    aidlName := DefaultAaidlName,
    dxName := DefaultDxName,
    manifestName := DefaultAndroidManifestName,
    jarName := DefaultAndroidJarName,
    mapsJarName := DefaultMapsJarName,
    assetsDirectoryName := DefaultAssetsDirectoryName,
    resDirectoryName := DefaultResDirectoryName,
    classesMinJarName := DefaultClassesMinJarName,
    classesDexName := DefaultClassesDexName,
    resourcesApkName := DefaultResourcesApkName,
    dxJavaOpts := DefaultDxJavaOpts,
    manifestSchema := DefaultManifestSchema,
    envs := DefaultEnvs
  )

  // Common case scenario for naming
  def commonAndroidSettingsIn(c: Configuration): Seq[Setting[_]] = Seq(
    managedJavaPath <<= (target in c)(_ / "src_managed" / "main" / "java"),
    makeManagedJavaPath in androidkey <<= AndroidHelpers.directory(managedJavaPath),
    classesMinJarPath <<= (target in c, classesMinJarName)(_ / _),
    packageApkPath in androidkey <<= (target in c, packageApkName in androidkey)(_ / _),
    resourcesApkPath in androidkey <<= (target in c, resourcesApkName)(_ / _),
    classesDexPath in androidkey <<= (target in c, classesDexName)(_ / _),
    nativeLibrariesPath in androidkey <<= (sourceDirectory in c)(_ / "libs"),
    packageApkName in androidkey := "test.apk"
  )
}