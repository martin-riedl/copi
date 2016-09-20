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

package copi.utils

import java.io._
import java.awt._
import java.awt.geom._
import java.awt.event._
import java.awt.image._
import javax.imageio._

import org.lwjgl.opengl.GL11
import java.nio._

object ImageUtil {
  def resize(src : BufferedImage,factor : Int) : BufferedImage = {
    val dest = new BufferedImage(src.getWidth()/factor,src.getHeight()/factor, BufferedImage.TYPE_INT_RGB)
	  val destGraphics = dest.createGraphics()
	  destGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
	  destGraphics.drawImage(src, 0, 0, dest.getWidth(), dest.getHeight(), 0, 0, src.getWidth(), src.getHeight(), null)  
    dest
  }
  
    /**
   * Return the Image pixels in default Java int ARGB format.
   * @return
   */
  def getImagePixels(image : Image) : Array[Int] = {
    val imgw = image.getWidth(null)
    val imgh = image.getHeight(null)
    var pixelsARGB = new Array[Int](imgw*imgh)
    val pg = new PixelGrabber(image, 0, 0, imgw, imgh, pixelsARGB, 0, imgw)
    pg.grabPixels()
    pixelsARGB
  }

  def loadImg(filename : String) : BufferedImage= {
    ImageIO.read(new File(filename))
  }
  
  def allocateTexture : Int = {
      val textureHandle : IntBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer()
      GL11.glGenTextures(textureHandle)
      textureHandle.get(0)
  }
  
      /**
   * Flip an array of pixels vertically
   * @param imgPixels
   * @param imgw
   * @param imgh
   * @return Array[Int]
   */
  def flipPixels(imgPixels : Array[Int], imgw : Int , imgh : Int ) : Array[Int] = {
    val flippedPixels = new Array[Int](imgw * imgh)
    for (y <- 0 to imgh-1; x <- 0 to imgw-1) {
      flippedPixels(( (imgh - y - 1) * imgw) + x) = imgPixels((y * imgw) + x)
    }
    return flippedPixels;
  }

   
  def convertImagePixelsRGBA(jpixels : Array[Int], imgw : Int , imgh : Int , flipVertically : Boolean) : ByteBuffer = {
      val jp = if (flipVertically) flipPixels(jpixels, imgw, imgh) else jpixels
      val bytes = convertARGBtoRGBA(jp)
      allocBytes(bytes);  // convert to ByteBuffer and return
  }
  
  def allocBytes(bytearray : Array[Byte]) : ByteBuffer = {
      val bb = ByteBuffer.allocateDirect(bytearray.length).order(ByteOrder.nativeOrder());
      bb.put(bytearray).flip();
      return bb;
  }

  /**
   * Convert pixels from java default ARGB int format to byte array in RGBA format.
   * @param jpixels
   * @return
   */
  def convertARGBtoRGBA(jpixels : Array[Int]) : Array[Byte] = {
      val bytes = new Array[Byte](jpixels.length*4)  // will hold pixels as RGBA bytes
      var j=0;
      for (i <- 0 to jpixels.length-1) {
          val p = jpixels(i)
          val a = ((p >> 24) & 0xFF).toByte  // get pixel bytes in ARGB order
          val r = ((p >> 16) & 0xFF).toByte
          val g = ((p >> 8) & 0xFF).toByte
          val b = ((p >> 0) & 0xFF).toByte
          bytes(j+0) = r  // fill in bytes in RGBA order
          bytes(j+1) = g
          bytes(j+2) = b
          bytes(j+3) = a
          j += 4;
      }
      bytes
  }

  
  def makeTexture(pixelsARGB : Array[Int], w : Int , h : Int, anisotropic : Boolean) : Int = {
  	val pixelsRGBA : ByteBuffer = convertImagePixelsRGBA(pixelsARGB,w,h,false);
    makeTexture(pixelsRGBA, w, h, anisotropic);
  	return 0;
  }
  
      /**
   * Return true if the OpenGL context supports the given OpenGL extension.
   *
   * This function uses glGetString() to get the extensions, then parses
   * the list.  This is inefficient, so should not be called often.
   * Check for the extensions you need in your init() function, and
   * then set flags accordingly.
   */
  def extensionExists(extensionName : String) = {
    val extensions = GL11.glGetString(GL11.GL_EXTENSIONS)
    val GLExtensions = extensions.split(" ")
  	val extensionNameUpper = extensionName.toUpperCase()
    
    val result = for {
      i <- 0 to GLExtensions.length-1
      if (GLExtensions(i).toUpperCase().equals(extensionNameUpper)) 
    } yield true
    
    if (result.length>0) true else false
  }

  def makeTexture( pixels : ByteBuffer,  w: Int , h : Int, anisotropic : Boolean) = {     // get a new empty texture
    val textureHandle = allocateTexture
    // preserve currently bound texture, so glBindTexture() below won't affect anything)
    GL11.glPushAttrib(GL11.GL_TEXTURE_BIT)
    // 'select' the new texture by it's handle
    GL11.glBindTexture(GL11.GL_TEXTURE_2D,textureHandle)
    // set texture parameters
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR) //GL11.GL_NEAREST);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR) //GL11.GL_NEAREST); : Int , h : Int , anisotropic : Boolean) : Int = {
    // get a new empty texture
    val textureHandle2 = allocateTexture
    // preserve currently bound texture, so glBindTexture() below won't affect anything)
    GL11.glPushAttrib(GL11.GL_TEXTURE_BIT)
    // 'select' the new texture by it's handle
    GL11.glBindTexture(GL11.GL_TEXTURE_2D,textureHandle2)
    // set texture parameters
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR) //GL11.GL_NEAREST)
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR) //GL11.GL_NEAREST)
     
    import org.lwjgl.opengl.EXTTextureFilterAnisotropic
    import org.lwjgl.opengl._
      // make texture "anisotropic" so it will minify more gracefully
  	if (anisotropic && extensionExists("GL_EXT_texture_filter_anisotropic")) {
  	  
     
  		// Due to LWJGL buffer check, you can't use smaller sized buffers (min_size = 16 for glGetFloat()).
  		val max_a = FloatBuffer.allocate(16)
  		// Grab the maximum anisotropic filter.
  		GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max_a)
  		// Set up the anisotropic filter.
  		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, max_a.get(0));
  	}

    // Create the texture from pixels
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
    		0, 						// level of de	tail
    		GL11.GL_RGBA8,			// internal format for texture is RGB with Alpha
    		w, h, 					// size of texture image
    		0,						// no border
    		GL11.GL_RGBA, 			// incoming pixel format: 4 bytes in RGBA order
    		GL11.GL_UNSIGNED_BYTE,	// incoming pixel data type: unsigned bytes
    		pixels)			// incoming pixels

    // restore previous texture settings
    GL11.glPopAttrib()

    textureHandle2
  }
}

object GLUtils {
  def pushAttribOrtho = GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_DEPTH_BUFFER_BIT)
  def popAttrib = GL11.glPopAttrib() 
}

object Helper {
  def getPowerOfTwoBiggerThan(n : Int)  : Int = {
      var x = n
      if (x < 0)
          return 0 
      else {
        x-=1
        x |= x >> 1
        x |= x >> 2
        x |= x >> 4
        x |= x >> 8
        x |= x >> 16
        x+1
      }
  }
}
