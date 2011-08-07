import sbt._
import classpath._
import scala.xml._

import Keys._
import AndroidKeys._
import AndroidHelpers._

object AndroidInstrumentation {

  private def instrument = (dbPath in Android, manifestSchema in Android, manifestPackage in Android,
    manifestPath in Android, instrumentationTestRunner in Android, streams) map {
      (dp, schema, mPackage, amPath, tr, s) ⇒
        s.log.info("Running instrumentation: " + "shell am instrument -w %s/%s" format (mPackage, tr))
        adbTask(dp.absolutePath,
          false,
          "shell am instrument -w " + mPackage + "/" +
            tr)
        ()
    }

  lazy val settings: Seq[Setting[_]] = inConfig(Android)(AndroidProject.androidSettings ++ Seq(something <<= { instrument }))

  lazy val InstrumentationTest = config("instrumentation") extend (IntegrationTest, Android)

  lazy val instrumentationSettings: Seq[Setting[_]] = inConfig(InstrumentationTest)(
    AndroidProject.androidSettings ++ Defaults.testSettings ++ AndroidInstrumentation.settings ++ Defaults.itSettings)

  lazy val defaultAliases = Seq(aaptGenerate, packageDebug)

  def coffeeSettings: Seq[Setting[_]] = inConfig(InstrumentationTest)(
    Defaults.testSettings ++
      Seq(

        sourceDirectory <<= (baseDirectory in Runtime) { _ / "src" / "it" },
        resourceDirectory <<= (resourceDirectory in Compile).identity,
        test <<= test dependsOn (installDevice in InstrumentationTest),
        resourcesApkName := "resources-test.xap",
        name := "InstrumentationTest",
        packageApkName := "teste")
        ++

        AndroidProject.androidSettings ++ AndroidInstall.settings

        ++ defaultAliases.map(
          k ⇒ k <<= (k in Android).identity)
          
          )

}