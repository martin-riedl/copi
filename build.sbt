libraryDependencies += "org.apache.sanselan" % "sanselan" % "0.97-incubator"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.11.8"

resolvers += "swt-repo" at "https://github.com/maven-eclipse/maven-eclipse.github.io/tree/master/maven/" 

libraryDependencies += "org.eclipse.swt" % "org.eclipse.swt.gtk.linux.x86_64" % "4.3"

seq(lwjglSettings: _*)

lazy val commonSettings = Seq(
  version := "0.1",
  organization := "xyz.riedl",
  scalaVersion := "2.11.8"
)

lazy val copi = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    mainClass in assembly := Some("copi.main")
  )



