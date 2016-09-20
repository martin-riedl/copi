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

package copi.logic

import java.awt.image.BufferedImage
import copi.utils._

sealed abstract class COPI_BASE

/**
 * represents all timelines currently added
 */
case class COPI_Project(tls : Array[COPI_Timeline]) extends COPI_BASE {
  val medianTimeInSeconds =  (BigInt(0) /: tls) ((a,b) => a + b.medianTimeInSeconds) / tls.length 
  val minTimeInSeconds = (tls.head.minTimeInSeconds /: tls) ((a,b) => {
    if (a<b.minTimeInSeconds) a else b.minTimeInSeconds
  })
  val maxTimeInSeconds = (tls.head.maxTimeInSeconds /: tls) ((a,b) => {
    if (a>b.maxTimeInSeconds) a else b.maxTimeInSeconds
  })
  //print("Project: ")
  //println(minTimeInSeconds, medianTimeInSeconds, maxTimeInSeconds)
  
  
  var offset = 0.5
  
  def setOffset(os : Double) {
    offset = os
  }
    
  var active = 0
  tls(active).active=true

  /**
   * sets the time drift in seconds to the currently active timeline
   * @param t normalized time drift (-1..0..1)
   */
  def setTimeTranslation(t : Double) = {
    tls(active).modTimeDiffSeconds=((maxTimeInSeconds-minTimeInSeconds).doubleValue*t).toInt
  }
  
  /**
   * retrieves the normalized time drift of the currently active timeline
   * @returns normalized time drift (-1..0..1)
   */
  def getTimeTranslation() : Double = {
    (tls(active).modTimeDiffSeconds.doubleValue / (maxTimeInSeconds-minTimeInSeconds).doubleValue)
  }
  
  /**
   * activetes the predecessor of the currently active timeline
   * @returns normalized time drift (-1..0..1)
   */
  def activatePred() : Double = {
    var i=1
    do {
      if (tls(i%tls.length).active) {
        tls(i % tls.length).active=false
        tls((i-1)%tls.length).active=true
        active = (i-1)%tls.length
        i=tls.length+1
      } else i+=1
    } while (i<=tls.length)
      
    getTimeTranslation() 
  }
  
  /**
   * activetes the successor of the currently active timeline
   * @returns normalized time drift (-1..0..1)
   */
  def activateNext() : Double = {
    var i=0 
    do {
      if (tls(i%tls.length).active) {
        tls(i%tls.length).active=false
        tls((i+1)%tls.length).active=true
        active = (i+1)%tls.length
        i=tls.length
      } else i+=1
    } while (i<tls.length)
      
    getTimeTranslation()
  }
}

/**
 * represents all image within a directory coming from a digital camera
 */
case class COPI_Timeline(directory : String, imgs : Array[COPI_Image]) extends COPI_BASE {
  assert(imgs.length!=0)
  var active = false
  var modTimeDiffSeconds = 0
  
  val minTimeInSeconds = (BigInt(imgs.head.date.getTime()) /: imgs) ((a,b) => {
    val imgDateInSec= BigInt(b.date.getTime()/ 1000)
    if (a<imgDateInSec) a else imgDateInSec
  })
  
  val maxTimeInSeconds = (BigInt(0) /: imgs) ((a,b) => {
    val imgDateInSec= BigInt(b.date.getTime()/ 1000)
    if (a>imgDateInSec) a else imgDateInSec
  })
  
  val medianTimeInSeconds = (BigInt(0) /: imgs) ((a,b) => {
    val imgDateInSec= BigInt(b.date.getTime()/ 1000)
    (a + imgDateInSec) 
  }) / imgs.length
 
  //print("Timeline: ")
  //println(minTimeInSeconds, medianTimeInSeconds, maxTimeInSeconds)
}

/**
 * represents the filename, date and thumpnail of a certain image
 */
case class COPI_Image(filename : String, date : java.util.Date, image : BufferedImage) extends COPI_BASE {
  val height : Int = image.getHeight()
  val width : Int = image.getWidth()  
  val widthRatio : Float = 1.0f
  val heightRatio : Float = 1.0f
  val pixels = ImageUtil.flipPixels(ImageUtil.getImagePixels(image), image.getWidth, image.getHeight)
  var id : Int = -1
  val dateTimeInSeconds = BigInt(date.getTime()/1000)
}
