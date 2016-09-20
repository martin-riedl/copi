/**
 * COPI is a tool for setting a timedrift between different 
 * image sets by its exif-information and sort them chronologically
 * 
 * Copyright (C) 2009  Martin Riedl
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package copi 

import logic._
import rendering._

import java.util._
import java.io._
import java.net.URISyntaxException
import org.lwjgl.LWJGLUtil

object main extends SICApplicationLogic {
    
/*
    def init() = {
		val osName = System.getProperty("os.name");
		// Get .jar dir. new File(".") and property "user.dir" will not work if
		// .jar is called from
		// a different directory, e.g. java -jar /someOtherDirectory/myApp.jar
		var nativeDir = "";
		try {
			nativeDir = new File(this.getClass().getProtectionDomain()
					.getCodeSource().getLocation().toURI()).getParent();
		} catch {
            case uriEx : URISyntaxException =>
			    try {
				    // Try to resort to current dir. May still fail later due to bad
				    // start dir.
				    uriEx.printStackTrace();
				    nativeDir = new File(".").getCanonicalPath();
			    } catch {
                    case ioEx : IOException => 
                        println("Failed to locate native library directory. Error:\n" + ioEx.toString());
        				ioEx.printStackTrace();
	        			System.exit(-1);
			    }
		}

		var lwjgldir = nativeDir;
		// Append library subdir
		lwjgldir += File.separator + "lib" + File.separator + "lwjgl" + File.separator + "native" + File.separator;

		if (osName.startsWith("Windows")) {
			lwjgldir += "windows"
		} else if (osName.startsWith("Linux") || osName.startsWith("FreeBSD")) {
			lwjgldir += "linux"
		} else if (osName.startsWith("Mac OS X")) {
			lwjgldir += "macosx"
		} else if (osName.startsWith("Solaris") || osName.startsWith("SunOS")) {
			lwjgldir += "solaris"
		} else {
			println("Unsupported OS: " + osName + ". Exiting.")
			System.exit(-1);
		}

        println(lwjgldir)

		System.setProperty("org.lwjgl.librarypath", lwjgldir)
	}*/


    def addLwjglNativesToJavaLibraryPathProperty() = {
        val osId = System.getProperty("os.name")
        println(osId);

        val dirOS = osId match {
            case "Windows" => "windows"
            case "Linux" => "linux"
            case "Max OS X" => "macosx"
            case "Solaris" => "solaris"
            case sthelse => println("Unsupported OS: " + sthelse + ". Exiting.");System.exit(-1);""
        }

        addPathToJavaLibraryPathProperty("/home/riedl/Arbeit/git/copi/lib/lwjgl/native/" + dirOS);
    }

    // http://stackoverflow.com/q/5419039
    def addPathToJavaLibraryPathProperty(propertyValue : String) : Unit = {
        println("Martin: "+propertyValue);
        val propertyName = "java.library.path"
        try {
            val field = classOf[ClassLoader].getDeclaredField("usr_paths")
            field.setAccessible(true)
            val paths = field.get(null).asInstanceOf[Array[String]]
            for (path <- paths) {
                if (propertyValue.equals(path)) return;
            }

            val tmp = new Array[String](paths.length + 1)
            System.arraycopy(paths, 0, tmp, 0, paths.length)
            tmp(paths.length) = propertyValue
            field.set(null, tmp)
            println(propertyName + " " + System.getProperty(propertyName) + File.pathSeparator + propertyValue)
            System.setProperty(propertyName, System.getProperty(propertyName) + File.pathSeparator + propertyValue)
        } catch {
            case e : IllegalAccessException => throw new RuntimeException("Failed to get permissions to set " + propertyName);
            case e : NoSuchFieldException => throw new RuntimeException("Failed to get field handle to set " + propertyName);
        }
    }


    def anotherOne() = {
        val jgllib = LWJGLUtil.getPlatform() match 
        {
            case LWJGLUtil.PLATFORM_WINDOWS => new File("./lib/lwjgl/native/windows/")
            case LWJGLUtil.PLATFORM_LINUX => new File("./lib/lwjgl/native/linux/")
            case LWJGLUtil.PLATFORM_MACOSX => new File("./lib/lwjgl/native/macosx/")
        }

        System.setProperty("org.lwjgl.librarypath", jgllib.getAbsolutePath());
    }
  

  def main(args : Array[String]) : Unit = {
    println("run renderer")
//    init()
    //addLwjglNativesToJavaLibraryPathProperty()
     //anotherOne()
    //System.setProperty("org.lwjgl.librarypath", "./");
    render	    
  }
}



