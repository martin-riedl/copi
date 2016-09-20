libraryDependencies += "org.apache.sanselan" % "sanselan" % "0.97-incubator"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.11.8"

//libraryDependencies += "org.eclipse" % "swt" % "3.5.1-v3555a"

//libraryDependencies += "org.eclipse.swt.gtk.linux" % "x86_64" % "3.5.1-v3555a"

resolvers += "swt-repo" at "https://github.com/maven-eclipse/maven-eclipse.github.io/tree/master/maven/" 

//libraryDependencies += "org.eclipse.swt" % "org.eclipse.swt.gtk.linux.x86_64" % "3.5.1" from "https://github.com/maven-eclipse/swt-repo/raw/master/org/eclipse/swt/org.eclipse.swt.gtk.linux.x86_64/3.5.1/org.eclipse.swt.gtk.linux.x86_64-3.5.1.jar"

//libraryDependencies += "org.mod4j.org.eclipse" % "swt" % "3.5.0"

libraryDependencies += "org.eclipse.swt" % "org.eclipse.swt.gtk.linux.x86_64" % "4.3"

name := "copi"

version := "0.1"

scalaVersion := "2.11.8"

seq(lwjglSettings: _*)
