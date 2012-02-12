import sbt._
import Keys._

import AndroidKeys._
import AndroidHelpers._

object AndroidTest {

  val DefaultInstrumentationRunner = "android.test.InstrumentationTestRunner"

  def instrumentationTestAction(emulator: Boolean) = (dbPath, manifestPackage, instrumentationRunner, streams) map {
    (dbPath, manifestPackage, inst, s) =>
      val action = Seq("shell", "am", "instrument", "-w",
        manifestPackage + "/" + inst)
      adbTask(dbPath.absolutePath, emulator, s, action: _*)
  }


  /**AndroidTestProject */
  lazy val androidSettings = settings ++
    inConfig(Android)(Seq(
      proguardInJars <<= (scalaInstance) map {
        (scalaInstance) =>
          Seq(scalaInstance.libraryJar)
      }
    )
    )


  lazy val settings: Seq[Setting[_]] =
    AndroidBase.settings ++
      AndroidInstall.settings ++
      inConfig(Android)(Seq(
        instrumentationRunner := DefaultInstrumentationRunner,
        testEmulator <<= instrumentationTestAction(true),
        testDevice <<= instrumentationTestAction(false)
      )) ++ Seq(
      testEmulator <<= (testEmulator in Android),
      testDevice <<= (testDevice in Android)
    )
}
