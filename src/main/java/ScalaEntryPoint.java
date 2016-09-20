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

package copi;

import java.util.*;
import java.io.*;
import java.net.URISyntaxException;

public class ScalaEntryPoint {
	public void init() {
		String osName = System.getProperty("os.name");
		// Get .jar dir. new File(".") and property "user.dir" will not work if
		// .jar is called from
		// a different directory, e.g. java -jar /someOtherDirectory/myApp.jar
		String nativeDir = "";
		try {
			nativeDir = new File(this.getClass().getProtectionDomain()
					.getCodeSource().getLocation().toURI()).getParent();
		} catch (URISyntaxException uriEx) {
			try {
				// Try to resort to current dir. May still fail later due to bad
				// start dir.
				uriEx.printStackTrace();
				nativeDir = new File(".").getCanonicalPath();
			} catch (IOException ioEx) {
				// Completely failed
				System.out
						.println("Failed to locate native library directory. Error:\n"
								+ ioEx.toString());
				ioEx.printStackTrace();
				System.exit(-1);
			}
		}
		String lwjgldir = nativeDir;
		// Append library subdir
		lwjgldir += File.separator + "lib" + File.separator + "lwjglnative"
				+ File.separator;
		if (osName.startsWith("Windows")) {
			lwjgldir += "windows";
		} else if (osName.startsWith("Linux") || osName.startsWith("FreeBSD")) {
			lwjgldir += "linux";
		} else if (osName.startsWith("Mac OS X")) {
			lwjgldir += "macosx";
		} else if (osName.startsWith("Solaris") || osName.startsWith("SunOS")) {
			lwjgldir += "solaris";
		} else {
			System.out.println("Unsupported OS: " + osName + ". Exiting.");
			System.exit(-1);
		}
		System.setProperty("org.lwjgl.librarypath", lwjgldir);
	}

	public static void main(String[] args) {
		/*
		 * Set lwjgl library path so that LWJGL finds the natives depending on
		 * the OS.
		 */
        System.out.println("Starting COPI ...");

		(new ScalaEntryPoint()).init();
		List<String> argList = new ArrayList<String>();
		argList.add("copi.main");
		for (String s : args)
			argList.add(s);
		scala.tools.nsc.MainGenericRunner.main(argList.toArray(new String[0]));

	}
}
