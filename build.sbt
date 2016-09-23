libraryDependencies += "org.apache.sanselan" % "sanselan" % "0.97-incubator"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.11.8"

resolvers += "swt-repo" at "https://github.com/maven-eclipse/maven-eclipse.github.io/tree/master/maven/" 

libraryDependencies += "org.eclipse.swt" % "org.eclipse.swt.gtk.linux.x86_64" % "4.3"

seq(lwjglSettings: _*)

// A fork of native-utils from https://github.com/adamheinrich/native-utils that includes a build.sbt in addition to the pom.xml. This allows to use it as a sbt dependency. 
//lazy val nativeUtils = ProjectRef(uri("git://github.com/phdoerfler/native-utils#master"), "native-utils") 

lazy val commonSettings = Seq(
  version := "0.1",
  organization := "xyz.riedl",
  scalaVersion := "2.11.8"
)

lazy val copi = (project in file(".")).
//  dependsOn(nativeUtils).
  settings(commonSettings: _*).
  settings(
    mainClass in Compile := Some("copi.DefaultStarter"),
    mainClass in assembly := Some("copi.ScalaEntryPoint")  
  )

seq(lwjglSettings: _*)

