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

import copi.rendering._
import copi.utils._

/**
 * here the application logic is implemented
 * - adding a directory to a project
 * - generating a new timeline
 */
trait SICApplicationLogic extends GLRenderable {
  /**
   * addNewDirectory adds to the already existing project a timeline (specified by the directory)
   */
   def addNewDirectory(directory : String) = {
	  val loadingprocess = new Runnable() {
	    def run() = {
	      setTextEntry("adding directory\n")
	      generateTimeline(directory) match {
	        case Some(newtl : COPI_Timeline) => {
	          getSceneGraph match {
	            case Some(p : COPI_Project) => {
	              val already_existing_timelines = p.tls.filter(a=> a.directory != directory)
	              setSceneGraph(Some(COPI_Project((already_existing_timelines.toList ::: List(newtl)).toArray)))
	            }
	            case None => setSceneGraph(Some(COPI_Project(List(newtl).toArray)))
	            case Some(noprojectobject) => println("scene graph object found but not of type COPI_Project to apply timeline")
	          }
	        }
	        case None => println("Couldn't generate new timeline")
	      }		    
	    }
	  }
	  new Thread(loadingprocess).start
  }

  /**
   * generateTimeline generates a timeline specified by the directory
   */
   def generateTimeline(directory : String) : Option[COPI_Timeline] = {
	  val sic_images = for (entry <- prepare(directory).filter(a => a._2.isDefined)) yield COPI_Image(entry._1,entry._2.get,entry._3)
	  if (sic_images.length>0) Some(COPI_Timeline(directory,sic_images)) else None
   }
}
