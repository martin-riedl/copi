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

package copi.rendering

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GLContext
import org.lwjgl.util.glu.GLU
import org.lwjgl.LWJGLException
import org.lwjgl.BufferUtils
import org.lwjgl.util._

import java.nio.ByteOrder
import java.nio.ByteBuffer
import java.nio.IntBuffer

import scala.math._

import copi.logic._
import copi.utils._

object GLRenderer {
  private var normtime = BigInt(3600) 										// timespan normalized to 1
  private var scalefak = 1.0          										// scaling from GUI
  private var timefaktor = normtime.doubleValue()*scalefak	// 
  
  /**
   * sets the timescale 
   * @param timescale
   */
  def setTimeScale(timescale : Double) {
    timescale match {
      case x : Double if (x>0) => {
        scalefak=1/x
        timefaktor = normtime.doubleValue()*scalefak
      }
    }
  }
  
  /**
   * renders the scene graph object given 
   * @param elem scene graph object
   */
  def render(elem : COPI_BASE) : Unit = elem match {
    case p:COPI_Project => {
      
      val difftimeright	= (p.minTimeInSeconds-p.medianTimeInSeconds).doubleValue
      val difftimeleft	= (p.maxTimeInSeconds-p.medianTimeInSeconds).doubleValue
      normtime = p.maxTimeInSeconds - p.minTimeInSeconds
      
      var y_trans = p.tls.length.toFloat/2 // defines the upper y value

      // determines what we can see!!!
      //println((difftimeleft + p.offset * normtime.doubleValue)/timefaktor)
      GL11.glTranslated((-difftimeright + p.offset*(difftimeright-difftimeleft))/timefaktor, 0.0, 0.0)

      //main.printgl( 40, 210, "Text rendered with the default GLApp.print() function uses")
      //val font = new GLFont(new java.awt.Font("Trebuchet", Font.BOLD, 18) )
      /*
      font.print(5, 5, "Type something:")
      font.print(1, 1, "Type something:")
      font.print(-3, -3, "Type something:")
      font.print(-3, 3, "Type something:")
      */
      
      GL11.glColor3d(0.35,0.35,0.35)
      GL11.glBegin(GL11.GL_QUADS)
      	GL11.glVertex3d(difftimeleft/timefaktor,y_trans+1,0)          
      	GL11.glVertex3d(difftimeright/timefaktor,y_trans+1,0)   
      	GL11.glVertex3d(difftimeright/timefaktor,-y_trans,0)          
      	GL11.glVertex3d(difftimeleft/timefaktor,-y_trans,0)  
      GL11.glEnd()
      
      for (tl <- p.tls) {
        
        // draw original timeline marker
        val xtranslation_withoutmod = (tl.medianTimeInSeconds - p.medianTimeInSeconds).doubleValue()/timefaktor
        GL11.glPushMatrix()
        	GL11.glTranslated(xtranslation_withoutmod,y_trans,0.0f)
        	GL11.glColor3d(0.1,0.1,0.1)
        	GL11.glBegin(GL11.GL_QUADS)
        		GL11.glVertex3d((tl.maxTimeInSeconds-tl.medianTimeInSeconds).doubleValue()/timefaktor,0.5,0)
        		GL11.glVertex3d((tl.minTimeInSeconds-tl.medianTimeInSeconds).doubleValue()/timefaktor,0.5,0)  
        		GL11.glVertex3d((tl.minTimeInSeconds-tl.medianTimeInSeconds).doubleValue()/timefaktor,-0.5,0)
        		GL11.glVertex3d((tl.maxTimeInSeconds-tl.medianTimeInSeconds).doubleValue()/timefaktor,-0.5,0)  
        	GL11.glEnd()
        GL11.glPopMatrix()

        // draw time modified timeline
        val xtranslation = (tl.medianTimeInSeconds - tl.modTimeDiffSeconds - p.medianTimeInSeconds).doubleValue()/timefaktor
        
        GL11.glPushMatrix()
        	GL11.glTranslated(xtranslation,y_trans,0.01f)
        	render(tl)
        GL11.glPopMatrix()
        
        y_trans-=1.0f
      }
    }
    
    case tl : COPI_Timeline => {  
      GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK,GL11.GL_FILL)
      if (tl.active) GL11.glColor4d(0.0,0.0,0.5,1.0) else GL11.glColor4d(0.3,0.3,0.3,1.0)
      // minTime visualization line
      GL11.glBegin(GL11.GL_LINE)
      GL11.glVertex3d((tl.minTimeInSeconds-tl.medianTimeInSeconds).doubleValue()/timefaktor,1.0,0)  
      GL11.glVertex3d((tl.minTimeInSeconds-tl.medianTimeInSeconds).doubleValue()/timefaktor,-1.0,0)
      GL11.glEnd()
      // maxTime visualization line
      GL11.glBegin(GL11.GL_LINE)
      GL11.glVertex3d((tl.maxTimeInSeconds-tl.medianTimeInSeconds).doubleValue()/timefaktor,1.0,0)  
      GL11.glVertex3d((tl.maxTimeInSeconds-tl.medianTimeInSeconds).doubleValue()/timefaktor,-1.0,0)
      GL11.glEnd()
      // timeline visualisation quad
      GL11.glBegin(GL11.GL_QUADS)
      GL11.glVertex3d((tl.maxTimeInSeconds-tl.medianTimeInSeconds).doubleValue()/timefaktor,0.5,0)
      GL11.glVertex3d((tl.minTimeInSeconds-tl.medianTimeInSeconds).doubleValue()/timefaktor,0.5,0)  
      GL11.glVertex3d((tl.minTimeInSeconds-tl.medianTimeInSeconds).doubleValue()/timefaktor,-0.5,0)
      GL11.glVertex3d((tl.maxTimeInSeconds-tl.medianTimeInSeconds).doubleValue()/timefaktor,-0.5,0)  
      GL11.glEnd()
      
      for (img <- tl.imgs) {               
        val imgtime = img.dateTimeInSeconds
        val medtime = tl.medianTimeInSeconds
        val xtranslation = (imgtime-medtime).doubleValue()/timefaktor

        GL11.glPushMatrix()
        GL11.glTranslated(xtranslation-0.5,-0.5,0.31f)
        render(img)
        GL11.glPopMatrix()
      }       
    }
    
    case img : COPI_Image => {    
     
     
     GL11.glColor4d(1.0,1.0,1.0,1.0)
     
     GL11.glBegin(GL11.GL_LINE)
     GL11.glVertex3d(img.widthRatio/2,0.0,0)  
     GL11.glVertex3d(img.widthRatio/2,img.heightRatio,0)
     GL11.glEnd()
     
     bind(elem) // bind current image texture 
     
     GL11.glEnable(GL11.GL_TEXTURE_2D)

     GL11.glBegin(GL11.GL_QUADS)
	   GL11.glTexCoord2f(0.0f, 0.0f);						GL11.glVertex2d(0.1, 0.1)
	   GL11.glTexCoord2f(img.widthRatio, 0.0f);        		GL11.glVertex2d(-0.1+img.width/img.width, 0.1);
	   GL11.glTexCoord2f(img.widthRatio, img.heightRatio);	GL11.glVertex2d(-0.1+img.width/img.width, img.height/img.height-0.1);
	   GL11.glTexCoord2f(0.0f, img.heightRatio);			GL11.glVertex2d(0.1, img.height/img.height-0.1);
     GL11.glEnd()
     
     GL11.glDisable(GL11.GL_TEXTURE_2D)

    }
  }
  
  /**
   * binds a texture to opengl for a given scene graph object
   * @elem scene graph object
   */
  def bind(elem : COPI_BASE) : Unit = {
    elem match {
      // not bound yet
      case img : COPI_Image => {
        if (img.id < 0) {
          // convert from argb into rgba format and writing in a bytebuffer for opengl
          val rgba_pixels = ImageUtil.convertARGBtoRGBA(img.pixels)
          val textureBuffer : ByteBuffer = ByteBuffer.allocateDirect(rgba_pixels.length).order(ByteOrder.nativeOrder) // * 4       
          for (i <- 0 to rgba_pixels.length-1) {
            textureBuffer.put(i, rgba_pixels(i))
          }
          
          // get the ID and bind the Texture
		      val idBuffer : IntBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer()
			    GL11.glGenTextures(idBuffer)
			    img.id = idBuffer.get(0)
			    GL11.glBindTexture(GL11.GL_TEXTURE_2D, img.id)
          
          // select modulate to mix texture with color for shading
			    GL11.glTexEnvf( GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE)
			    // when texture area is small, bilinear filter the closest mipmap
			    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
			    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, img.width, img.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureBuffer)
          // when texture area is large, bilinear filter the original
			    GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
			    // the texture wraps over at the edges (repeat)
			    GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
			    GL11.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)   
        } else {
          GL11.glBindTexture(GL11.GL_TEXTURE_2D, img.id)
        }
      }
      case _ => 
    }
  }
}

