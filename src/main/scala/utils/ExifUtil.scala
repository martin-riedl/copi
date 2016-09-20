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
import copi.logic.COPI_Timeline
import copi.logic.COPI_Image
import java.util.Date
import java.text.SimpleDateFormat
  
object ExifUtil {
  /**
   * applies to each image in a timeline the specified timedrift
   * @param timeline
   * @param timlineidentstring is an identifier for each timeline
   * @param image 
   * @param drifttimeseconds drift time to the original date 
   * @param destdir destination directory
   */
  def applyTimeToImage(timeline : COPI_Timeline, timelineidentstring : String , image : COPI_Image, driftimeseconds : Int, destdir : String) : Unit = {
    println(driftimeseconds)
    
    val actualtime = image.date.getTime()-driftimeseconds*1000
    val nd = new Date(actualtime)
    
    val formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    val newformateddate = formatter.format(nd)
    
    println(image.filename, formatter.format(image.date), newformateddate)
    val fs = new File(timeline.directory+"/"+image.filename)
    val fd = new File(destdir + "/"+ newformateddate + "_-_"+"_"+timelineidentstring+"_" + image.filename)

    writeExifDate(fs,fd,nd)
  }
  
  /**
   * extracts the exif information from a source file, 
   * modifies it by the date parameter
   * and writes it with the image information of the source file to the destination file
   * @param	fsrd	source file handle
   * @param fdst	destination file handle
   * @param date	data parameter
   */
  def writeExifDate(fsrc : File, fdst : File, date : Date) : Unit = {
    import org.apache.sanselan.formats.tiff.write._
    import org.apache.sanselan.formats.tiff.constants._
    
    val metadata = org.apache.sanselan.Sanselan.getMetadata(fsrc)
    val outputset = metadata match {
        case x : org.apache.sanselan.formats.jpeg.JpegImageMetadata => x.getExif().getOutputSet
    }
    
    val formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss\n")
    val fieldData = formatter.format(date)
    
    val imageDate = new TiffOutputField(
                         ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, 
                         TiffFieldTypeConstants.FIELD_TYPE_ASCII, 
                         fieldData.length, 
                         fieldData.getBytes)
    println(imageDate)
    val exifDirectory = outputset.getOrCreateExifDirectory
    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)
    exifDirectory.add(imageDate)
    
    val os = new BufferedOutputStream(new FileOutputStream(fdst))
    val rewriter = new org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter
    println(fsrc.toURI,os.toString)
    rewriter.updateExifMetadataLossless(fsrc,os, outputset)
  }
  
  /**
   ** reads the exif information of the specifiled file handle and returns the date extracted from the date field
   * @param f file handle
   * @return Some[Date] or None
   */
  def readExifDate(f : File) : Option[Date] = {
    val fd = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
    try {
      readExifDateOriginalAsCharSequence(f) match {
        case Some(dateseq) => println("DATETIMEORIGINAL:"+dateseq.toString);Some(fd.parse(dateseq.toString))
        case None => {
          readExifDateAsCharSequence(f) match {
            case Some(dateseq) => println("DATETIME:"+dateseq.toString);Some(fd.parse(dateseq.toString))
            case None => None
          }
        }
      }
    } catch {
      case e : Exception => None
    }    
  }
    
  /**
   * reads the exif information of the specified file handle and returns the date field
   * @param f file handle
   */
  private def readExifDateAsCharSequence(f : File) : Option[CharSequence] = {
    try {
      import  org.apache.sanselan.formats.tiff._
      
      val metadata = org.apache.sanselan.Sanselan.getMetadata(f)
      
      def dataextraction(tim : TiffImageMetadata) = {
        val field = tim.findField(constants.TiffTagConstants.TIFF_TAG_DATE_TIME)
        field.getStringValue.subSequence(0,19) // unterminated date-String -> therefore the subSequence call
      }
      
      metadata match {
        case x : org.apache.sanselan.formats.jpeg.JpegImageMetadata => Some(dataextraction(x.getExif()))
        case x : org.apache.sanselan.formats.tiff.TiffImageMetadata => Some(dataextraction(x))
      	case _ => throw new Exception {
      	  println(metadata)
      	  def unapply = "wrong metadata type"
      	}
      }
    } catch {
      case e  : Exception => {
        println("exif info couln't be extracted: ",e)
        None
      }
    }
  }
  
  
   
  /**
   * reads the exif information of the specified file handle and returns the date field
   * @param f file handle
   */
  private def readExifDateOriginalAsCharSequence(f : File) : Option[CharSequence] = {
    try {
      import  org.apache.sanselan.formats.tiff._
      
      val metadata = org.apache.sanselan.Sanselan.getMetadata(f)
      
      def dataextraction(tim : TiffImageMetadata) = {
        
        val field =   tim.findField(
          new constants.TagInfo(
          "DateTimeOriginal", 0x9003, 
          constants.TiffFieldTypeConstants.FIELD_TYPE_ASCII,1, 
          constants.TiffDirectoryConstants.EXIF_DIRECTORY_EXIF_IFD)
        )
        field.getStringValue.subSequence(0,19) // unterminated date-String -> therefore the subSequence call
       
      }
      
      metadata match {
        case x : org.apache.sanselan.formats.jpeg.JpegImageMetadata => Some(dataextraction(x.getExif()))
        case x : org.apache.sanselan.formats.tiff.TiffImageMetadata => Some(dataextraction(x))
      	case _ => throw new Exception {
      	  println(metadata)
      	  def unapply = "wrong metadata type"
      	}
      }
    } catch {
      case e  : Exception => {
        println("exif info couln't be extracted: ",e)
        None
      }
    }
  }
      
}
