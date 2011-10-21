package android

object AndroidPlugin extends sbt.Plugin {

  import sbt._
  import sbt.Keys._
  import AndroidKeys.{android => androidkey}
  import AndroidKeys._

  lazy val librarySettings: Seq[Setting[_]] = androidSettingsIn(Compile)



  def androidRobolectricSettingsIn(c:Configuration): Seq[Setting[_]] = inConfig(c)(Seq())

  def androidInstrumentationSettingsIn(c: Configuration): Seq[Setting[_]] = inConfig(c)(Seq())

  def androidSettingsIn(c: Configuration): Seq[Setting[_]] = inConfig(c)(Seq())


  def androidSettingsIn(c: Configuration): Seq[Setting[_]] =
    inConfig(c)(androidSettings0 ++ AndroidDefaults.commonAndroidSettingsIn(c) ++ Seq(

      amanifest in androidkey <<= (sourceDirectory in c)(_ / "AndroidManifest.xml"),
      pkg in androidkey <<= (organization),


      r in androidkey <<= aaptGenerateTask,
      r in androidkey <<= r in androidkey dependsOn (makeManagedJavaPath in androidkey),
      aidl in androidkey <<= aidlGenerateTask,
      res in androidkey in c <<= (sourceDirectory in c)(_ / "res"),

      sourceGenerators in c <+= r in androidkey,
      sourceGenerators in c <+= aidl in androidkey,

      addons in androidkey := Seq(),

      libraryJarPath <<= (jarPath in Android, addons in androidkey)(_ +++ _ get),

      unmanagedJars in c <++= libraryJarPath map (_.map(Attributed.blank(_))),


      //
      //      // disable .jar publishing
      //      publishArtifact in(c, packageBin) := false,
      //
      //      // create an Artifact for publishing the .war file
      //      artifact in(c, packageDebug) ~= {
      //        (art: Artifact) =>
      //          art.copy(`type` = "apk", extension = "apk")
      //      },
      //
      //      // add the .war file to what gets published
      //      addArtifact(artifact in(Compile, packageDebug), packageDebug),

      aaptPackage in c <<= aaptPackageTask,
      packageConfig <<=
        (toolsPath in Android,
          packageApkPath in androidkey,
          resourcesApkPath in androidkey,
          classesDexPath in androidkey,
          nativeLibrariesPath in androidkey,
          classesMinJarPath in androidkey,
          resourceDirectory in androidkey)
          (ApkConfig(_, _, _, _, _, _, _)),

      packageDebug <<= packageTask(true),
      sbt.Keys.`package` in c <<= packageDebug,
      sbt.Keys.`package` in market in c <<= packageRelease in androidkey,
      packageRelease in androidkey <<= packageTask(false)
    )) ++ Seq(
    )


  private def packageTask(debug: Boolean): Project.Initialize[Task[File]] = (packageConfig, streams) map {
    (c, s) =>
      val builder = new ApkBuilder(c, debug)
      builder.build.fold(s.log.error(_), s.log.info(_))
      s.log.debug(builder.outputStream.toString)
      c.packageApkPath
  }

  def lessSettings: Seq[Setting[_]] =
    AndroidInstallPath.settings ++ androidSettingsIn(Compile) ++ androidSettingsIn(Test)

  def androidSettings0: Seq[Setting[_]] =
    AndroidInstallPath.settings ++
      AndroidDefaults.settings ++ Seq(
      //r in androidkey <<= aaptGenerateTask
    )

  def androidProguardSettings: Seq[Setting[_]] = {
    Seq(
      //      proguardOptions ++= Seq(
      //        keepAllScala
      //      )
    )
  }

  private def aidlGenerateTask =
    (sourceDirectories, idlPath in Android, managedJavaPath, javaSource, streams) map {
      (sDirs, idPath, javaPath, jSource, s) =>
        generateAIDLFile(sDirs, idPath, javaPath, jSource, s.log)
    }

  private def generateAIDLFile(sources: Seq[File], aidlExecutable: File, outFolder: File, javaSourceFolder: File, out: Logger): Seq[File] = {
    val aidlPaths = sources.map(_ ** "*.aidl").reduceLeft(_ +++ _).get
    if (aidlPaths.isEmpty) {
      out.debug("no AIDL files found, skipping")
      Nil
    } else {
      val processor = aidlPaths.map {
        ap =>
          aidlExecutable.absolutePath ::
            "-o" + outFolder.absolutePath ::
            "-I" + javaSourceFolder.absolutePath ::
            ap.absolutePath :: Nil
      }.foldLeft(None.asInstanceOf[Option[ProcessBuilder]]) {
        (f, s) =>
          f match {
            case None => Some(s)
            case Some(first) => Some(first #&& s)
          }
      }.get
      out.debug("generating aidl " + processor)
      processor !

      val rPath = outFolder ** "R.java"
      outFolder ** "*.java" --- (rPath) get
    }
  }

  private def generateRFile(pkg: String, aapt: File, manifest: File, resFolder: File, androidJar: File, outFolder: File, out: Logger): Seq[File] = {
    val process = Process(
      <x>
        {aapt.absolutePath}
        package --auto-add-overlay -m --custom-package
        {pkg}
        -M
        {manifest.absolutePath}
        -S
        {resFolder.absolutePath}
        -I
        {androidJar.absolutePath}
        -J
        {outFolder.absolutePath}
      </x>)
    if (process ! out == 1) sys.error("Can not generate R file for command %s" format process.toString)
    outFolder ** "R.java" get
  }

  private def aaptGenerateTask =
    (pkg in androidkey, aaptPath in Android, amanifest in androidkey, res in androidkey, jarPath in Android, managedJavaPath, streams) map {
      (mPackage, aPath, mPath, resPath, jPath, javaPath, log) =>
        generateRFile(mPackage, aPath, mPath, resPath, jPath, javaPath, log.log)
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
        osDxName <<= (dxName)(_ + osBatchSuffix),
        platformPath <<= (sdkPath, platformName)(_ / "platforms" / _),
        toolsPath <<= (sdkPath)(_ / "tools"),
        dbPath <<= (platformToolsPath, adbName)(_ / _),
        platformToolsPath <<= (sdkPath)(_ / "platform-tools"),
        aaptPath <<= (platformToolsPath, aaptName)(_ / _),
        idlPath <<= (platformToolsPath, aidlName)(_ / _),
        dxPath <<= (platformToolsPath, osDxName)(_ / _),
        jarPath <<= (platformPath, jarName)(_ / _),

        sdkPath <<= (envs) {
          es =>
            determineAndroidSdkPath(es).getOrElse(sys.error(
              "Android SDK not found. You might need to set %s".format(es.mkString(" or "))
            ))
        }
      )
    }
  }

}