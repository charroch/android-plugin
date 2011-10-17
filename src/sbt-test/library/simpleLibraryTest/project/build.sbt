addSbtPlugin("org.scala-tools.sbt" % "sbt-android-plugin" % "0.6.0-SNAPSHOT")

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-proguard-plugin" % (v+"-0.1.1"))
