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


import copi.logic.*;
import copi.rendering.*;

import java.io.*;

public class ScalaEntryPoint {

	public static void main(String[] args) {
    /*
		 * Set lwjgl library path so that LWJGL finds the natives depending on
		 * the OS.
		 */
    String path = "libNativeLWJGL";
    File libDir = new File(path); 
    
    if (!libDir.exists()) {
      // create native lib folder 
      libDir.mkdir(); 
      
      // retrieve os type
      String osName = System.getProperty("os.name");
      
      // try to determine if the system is 64 bit  
      boolean is64bit = false;
      if (System.getProperty("os.name").contains("Windows")) {
          is64bit = (System.getenv("ProgramFiles(x86)") != null);
      } else {
          is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
      }
      
      // construct name of native lib file 
      String natLibLWJGL = ""; 
      if (osName.startsWith("Windows")) {
        natLibLWJGL += "lwjgl";
        if (is64bit) natLibLWJGL += "64";
        natLibLWJGL += ".dll";
      } else if (osName.startsWith("Linux")) {
        natLibLWJGL += "liblwjgl";
        if (is64bit) natLibLWJGL += "64";
        natLibLWJGL += ".so";
      } else if (osName.startsWith("Mac OS X")) {
        natLibLWJGL += "liblwjgl";
        natLibLWJGL += ".jnilib";
      } else {
        System.out.println("Unsupported OS: " + osName + ". Exiting.");
        System.exit(-1);
      }
      
      // try to establish an input stream on the native lib inside the jar
      InputStream fis = ScalaEntryPoint.class.getResourceAsStream("/"+natLibLWJGL);
      if (fis == null) {
          System.out.println("Native library file " + natLibLWJGL + " was not found inside JAR.");
          System.exit(-1);
      } 
      
      try {
        // establish an output stream on the target file 
        File fOut = new File(path + "/" + natLibLWJGL);
        FileOutputStream fos = new FileOutputStream(fOut);

        // create file at destination if not already existing
        if (!fOut.exists()) fOut.createNewFile();
        
        // making buffer for copy operation 
        byte[] buffer = new byte[1024];
        int readBytes;
   
        // Open output stream and copy data between source file in JAR and the temporary file
        try {
            while ((readBytes = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, readBytes);
            }
        } finally {
            fos.close();
            fis.close();
        }
      } catch (Exception e) {
        System.out.println(e.getMessage());
        System.exit(-1);  
      }
    }  
    
    // set lwjgl native library path
    System.setProperty("org.lwjgl.librarypath", libDir.getAbsolutePath());
    
    // start COPI
    System.out.println("Starting COPI ...");
    (new SICApplicationLogic()).render();
	}
}
