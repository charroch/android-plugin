package android

import _root_.android.AndroidHelpers._

object AndroidPlugin extends sbt.Plugin {

  import sbt._
  import sbt.Keys._
  import AndroidKeys.{android => androidkey}
  import AndroidKeys._

  lazy val librarySettings: Seq[Setting[_]] = AndroidInstallPath.settings ++ Seq(
    r in androidkey <<= aaptGenerateTask,
    amanifest in androidkey <<= (sourceDirectory)(_ / "AndroidManifest.xml"),
    pkg in androidkey <<= (organization),
    res in androidkey <<= (sourceDirectory)(_ / "res")
  )

  def androidSettingsIn(c: Configuration): Seq[Setting[_]] =
    inConfig(c)(androidSettings0 ++ Seq(
    )) ++ Seq(
      //      cleanFiles <+= (resourceManaged in lesskey in c).identity,
      //      watchSources <++= (unmanagedSources in lesskey in c).identity
    )

  def lessSettings: Seq[Setting[_]] =
    androidSettingsIn(Compile) ++ androidSettingsIn(Test)

  def androidSettings0: Seq[Setting[_]] = Seq(
    r in androidkey <<= aaptGenerateTask
  )

  private def aaptGenerateTask =
    (pkg in androidkey, aaptPath in Android, amanifest in androidkey, res in androidkey, jarPath in Android, resourceManaged in androidkey) map {
      (mPackage, aPath, mPath, resPath, jPath, javaPath) =>
        Process(<x>
          {aPath.absolutePath}
          package --auto-add-overlay -m
          --custom-package
          {mPackage}
          -M
          {mPath.absolutePath}
          -S
          {resPath.absolutePath}
          -I
          {jPath.absolutePath}
          -J
          {javaPath.absolutePath}
        </x>) !

        javaPath ** "R.java" get
    }

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
  }


  object AndroidInstallPath {

    def determineAndroidSdkPath(es: Seq[String]) = {
      val paths = for (e <- es; p = System.getenv(e); if p != null) yield p
      if (paths.isEmpty) None else Some(Path(paths.head).asFile)
    }

    def isWindows = System.getProperty("os.name").startsWith("Windows")

    def osBatchSuffix = if (isWindows) ".bat" else ""

    lazy val settings: Seq[Setting[_]] = inConfig(Android) {
      AndroidDefaults.settings ++ Seq(
        osDxName in androidkey <<= (dxName)(_ + osBatchSuffix),

        platformName := "android-10",
        platformPath <<= (sdkPath, platformName) (_ / "platforms" / _),

        toolsPath in androidkey <<= (sdkPath)(_ / "tools"),
        dbPath in androidkey <<= (platformToolsPath, adbName)(_ / _),
        platformToolsPath in androidkey <<= (sdkPath)(_ / "platform-tools"),
        aaptPath in androidkey <<= (platformToolsPath, aaptName)(_ / _),
        idlPath in androidkey  <<= (platformToolsPath, aidlName)(_ / _),
        dxPath in androidkey <<= (platformToolsPath, osDxName)(_ / _),
        jarPath <<= (platformPath, jarName) (_ / _),

        sdkPath in androidkey  <<= (envs) {
          es =>
            determineAndroidSdkPath(es).getOrElse(sys.error(
              "Android SDK not found. You might need to set %s".format(es.mkString(" or "))
            ))
        }
      )
    }
  }

}