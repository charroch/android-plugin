package android

import sbt._


object android {

  type Password = String
  type Alias = (String, Password)
  type Certificate = (File, Alias, Password)
  type Directory = File
  type Executable = File

  val Android = config("android")

  val packageApk = TaskKey[File]("package-apk", "Package the project into an APK")
  val debug = SettingKey[Certificate]("debug", "Certificate for debug signing")
  val market = SettingKey[Certificate]("market", "Certificate for market signing")
  val install = TaskKey[Unit]("install", "Install the APK")

  object sdk {
    val root = SettingKey[Directory]("sdk", "Android root installation directory")
    val dx = SettingKey[Executable]("dx", "dx executable path")
    val aapt = SettingKey[Executable]("aapt", "aapt executable path")
    val aidl = SettingKey[Executable]("aidl", "aidl executable path")
    val installedAddons = SettingKey[Map[String, ModuleID]]("addons", "Installed addons")
    val jars = SettingKey[Map[String, ModuleID]]("jars", "Installed android jars")

    lazy val settings: Seq[Setting[_]] = inConfig(Android)(Seq(
      android.sdk.root := file(System.getenv("ANDROID_HOME")),
      android.sdk.dx := android.sdk.root / "platform-tools" / "dx",
      android.sdk.aapt := android.sdk.root / "platform-tools" / "aapt",
      android.sdk.aidl := aidl,
      android.sdk.installedAddons := addons,
      android.sdk.jars := jars
    ))
  }

}


