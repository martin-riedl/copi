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

/*SWT - Stuff*/
import org.eclipse.swt._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.layout._
import org.eclipse.swt.widgets._
import org.eclipse.swt.opengl._
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Event
import org.eclipse.swt.widgets.Listener
import org.eclipse.swt.widgets.Shell

//opengl stuff
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GLContext
import org.lwjgl.util.glu.GLU
import org.lwjgl.LWJGLException

import copi.logic._
import copi.utils._

import java.io._

import scala.collection.immutable.Queue


abstract class GLRenderable {
  private var distance : Double = 1.0
  private var ytransl : Double = 0.0
  private var tex : Option[COPI_BASE] = None
  private var destdir : Option[String] = None
  private var progress : Option[Double] = None
  private var textentry = Queue[String]()
  
  /* Function Definitions */
  def addNewDirectory(directory : String)
  
  /* prepare a List of 3-Tuples with the path/filename, date and resized picture*/
  def prepare(directory : String) = synchronized {
    import java.awt.image._
		import javax.imageio._
		import java.awt._
		import java.awt.geom._
		import java.awt.image._
  
    val dir = new File(directory)
    var counter = 0.0
    val test = for {
      fstr <- dir.list()
      if (fstr.endsWith("jpg") || fstr.endsWith("JPG"))
    } yield {
      counter+=1
      setProgress(Some(counter/(dir.list().length).doubleValue))
      val filename = directory + "/"+ fstr
      setTextEntry("loading " + filename + "\n")
      //setTextEntry("loading " + filename)
      val src = ImageIO.read(new File(filename))
      //val dest = new BufferedImage(src.getWidth()/16,src.getHeight()/16, BufferedImage.TYPE_INT_RGB)
      val dest = new BufferedImage(128,128, BufferedImage.TYPE_INT_RGB)
      val destGraphics = dest.createGraphics()
      destGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON)
      destGraphics.drawImage(src, 0, 0, dest.getWidth(), dest.getHeight(), 0, 0, src.getWidth(), src.getHeight(), null)
      
      (fstr,ExifUtil.readExifDate(new File(filename)),dest)
    }
    
    setProgress(None)
    test
  }
  
  /**
   * add new scene graph to the renderer
   */
  def setSceneGraph(scenegraph : Option[COPI_BASE]) = synchronized {
    tex = scenegraph
  }
  
  /**
   * retrieves the renderers current scene graph
   */
  def getSceneGraph = synchronized {
    tex
  }

  /**
   * sets a new distance value 
   * @param d distance value
   */
  def setDistance(d : Double) = d match {
    case d if (d>0.0 && d<1) => distance = d
    case _ => distance = 1
  }
  
  /**
   * sets a new progress value
   * @param p progress value
   */
  def setProgress(p : Option[Double]) = synchronized {
    p match {
      case Some(v : Double) if v>=0 && v<=1 => {
        //prog_bar.setEnabled(true)
        progress = p
      }
      case _ => progress = None
    }
  }
  
    /**
   * sets a new progress value
   * @param p progress value
   */
  def setTextEntry(s : String) = synchronized {
    textentry.enqueue(s)
  }
  
  /* Initialisation of SWT */
  val display = new Display
  val shell = new Shell(display)
  shell.setText("COPI - Chronological Order Preparation of Images")
  shell.setSize(1024, 600)
  /************************* Listeners ******************************/
  class COPI_KeyListener extends KeyListener {
    var selectedItem = ""
    def keyPressed(e : KeyEvent) = {
      val key = Character.toString(e.character)
      e.keyCode match {
        case 16777217 => {
	        tex match {
	          case Some(p : COPI_Project) => mod_slider.setSelection((p.activatePred()*50000+50000).toInt)
	          case _ => setTextEntry("not a COPI_Project Element -> probably only a scene subgraph\n")
	        }            
        }
        
        case 16777218 => {
	        tex match {
	          case Some(p : COPI_Project) => mod_slider.setSelection((p.activateNext()*50000+50000).toInt)
	          case _ => setTextEntry("not a COPI_Project Element -> probably only a scene subgraph\n")
	        }        
        }
        
        case 16777219 => tex match {
          case Some(p : COPI_Project) => p.setTimeTranslation(p.getTimeTranslation()+0.00001)
          case _ => setTextEntry("not a COPI_Project Element -> probably only a scene subgraph\n")
        } 
        
        case 16777220 => tex match {
          case Some(p : COPI_Project) => p.setTimeTranslation(p.getTimeTranslation()-0.00001)
          case _ => setTextEntry("not a COPI_Project Element -> probably only a scene subgraph\n")
        }
        
        case 16777224 => ytransl-=0.1
        case 16777223 => ytransl+=0.1
        
        
        // remove selected timeline
        case 127 => tex match {
          case Some(p : COPI_Project) => synchronized {
            val timelines = p.tls.filter(a=> !a.active)
            if (timelines.length>0) setSceneGraph(Some(COPI_Project(timelines.toArray)))
            else setSceneGraph(None)
          }
          case _ => setTextEntry("not a COPI_Project Element -> probably only a scene subgraph\n")
        }
        
        case _ => println(e.keyCode.toString)
      }
      selectedItem = key
      //setTextEntry("Pressed: " + e.keyCode)/100000
    }

    def keyReleased(e : KeyEvent) {
      if (selectedItem.length() > 0) {
        //setTextEntry(("Release: " + selectedItem)
      } else {
        selectedItem = ""
      }
    }
  }
  
  class COPI_Mouse_Listener extends MouseWheelListener {
    def mouseScrolled(e : MouseEvent) =  {ytransl+=e.count/10.0}
  }
  
  val sic_resize_listener = new Listener {
		def handleEvent(event : org.eclipse.swt.widgets.Event) {
			bounds = canvas.getBounds()
			val fAspect = bounds.width / bounds.height
			canvas.setCurrent()
			try {
				GLContext.useContext(canvas)
			} catch {
				case e : LWJGLException => e.printStackTrace()	
			}
			GL11.glViewport(0, 0, bounds.width, bounds.height)
			GL11.glMatrixMode(GL11.GL_PROJECTION)
			GL11.glLoadIdentity()
			GLU.gluPerspective(45.0f, fAspect, 0.5f, 400.0f)
			GL11.glMatrixMode(GL11.GL_MODELVIEW)
			GL11.glLoadIdentity()
		}
  }
  
	val sic_selectionlistener = new Listener {
		def handleEvent (e : Event) {
			val d = dialog.open()
			if (d != null) addNewDirectory(d)    
		}
	}
  
  val sic_offset_sa = new SelectionAdapter {
    override def widgetSelected(e : SelectionEvent) = {
      val o = offset_slider.getSelection()
      //setTextEntry("Selection:"+o)
      tex match {
        case Some(p : COPI_Project) => p.setOffset(o.toDouble/100000)
        case _ => setTextEntry("not a COPI_Project Element -> probably only a scene subgraph\n")
      }
    }
  }
 	
  val sic_distance_sa = new SelectionAdapter {
    override def widgetSelected(e : SelectionEvent) = {
      val value = distance_slider.getSelection().doubleValue()/1000
      //setTextEntry("Selection:"+value)
      setDistance(value)
    }
  }
 
  val sic_timefak_sa = new SelectionAdapter() {
    override def widgetSelected(e : SelectionEvent) {
      val value = 1/Math.log(1+timefak_slider.getSelection().doubleValue()/1000000)
      //setTextEntry("time factor:"+ value)
      if (value>0)
        GLRenderer.setTimeScale(value)
    }
  }
  
  val sic_mod_sa = new SelectionAdapter {
    override def widgetSelected(e : SelectionEvent) = {
      val value = (mod_slider.getSelection()-50000).doubleValue/50000
      //setTextEntry("Timetranslation:"+value)
      tex match {
        case Some(p : COPI_Project) => p.setTimeTranslation(value)
        case _ => setTextEntry("not a COPI_Project Element -> probably only a scene subgraph\n")
      }
    }
  }
  
  /****************************** SWT Elements *****************************/
  // OpenGl Canvas
  val comp = new org.eclipse.swt.widgets.Composite(shell, SWT.NONE)
  
  val data = new GLData()
	data.doubleBuffer = true

  val canvas = new GLCanvas(comp, SWT.NONE, data)
  var bounds : Rectangle = canvas.getBounds()
	canvas.setCurrent()
	try {
		GLContext.useContext(canvas);
	} catch {
		case e : LWJGLException => e.printStackTrace()
  }

  canvas.addKeyListener(new COPI_KeyListener)
  canvas.addListener(SWT.Resize, sic_resize_listener)
  comp.addMouseWheelListener(new COPI_Mouse_Listener)
  
  // SWT Menue
  val bar = new org.eclipse.swt.widgets.Menu (shell, SWT.BAR)
  shell.setMenuBar(bar)
  val fileItem = new org.eclipse.swt.widgets.MenuItem (bar, SWT.CASCADE)
  fileItem.setText ("&Pictures")
  val submenu = new org.eclipse.swt.widgets.Menu (shell, SWT.DROP_DOWN)
  fileItem.setMenu(submenu)
  val item = new org.eclipse.swt.widgets.MenuItem (submenu, SWT.PUSH)
  item.setText ("Add &Sourcedirectory\tCtrl+S")
  item.setAccelerator (SWT.MOD1 + 'S')
  val dialog = new org.eclipse.swt.widgets.DirectoryDialog (shell, SWT.OPEN)
  item.addListener (SWT.Selection, sic_selectionlistener)

  val itemdestdir = new org.eclipse.swt.widgets.MenuItem (submenu, SWT.PUSH)
  itemdestdir.setText ("Set &Destinationdirectory\tCtrl+D")
  itemdestdir.setAccelerator (SWT.MOD1 + 'D')
  itemdestdir.addListener(SWT.Selection, new Listener {
    def handleEvent (e : Event) {
    	val d = dialog.open()
        if (d != null) {
	    	setTextEntry(d)
	    	destdir = Some(d)          
        } else destdir = None
    }
  })
  
  // Slider Composite
  val tools = new org.eclipse.swt.widgets.Composite(shell, SWT.BOTTOM)
  val timefak_slider = new Scale(tools,SWT.HORIZONTAL);
  timefak_slider.setMinimum(1)
	timefak_slider.setMaximum(100000)
	timefak_slider.setIncrement(1)
	timefak_slider.setPageIncrement(1)
	timefak_slider.setSelection(1000)
	timefak_slider.addSelectionListener(sic_timefak_sa)
  val tfLabel = new Label(tools, SWT.NULL)
	tfLabel.setText("TimeScaleSlider")

  val distance_slider = new Scale(tools,SWT.HORIZONTAL)
  distance_slider.setMinimum(1)
	distance_slider.setMaximum(1000)
	distance_slider.setIncrement(1)
	distance_slider.setPageIncrement(1)
	distance_slider.setSelection(500)
	distance_slider.addSelectionListener(sic_distance_sa)
  val distanceLabel = new Label(tools, SWT.NULL)
	distanceLabel.setText("DistanceSlider")

  val offset_slider = new Scale(tools,SWT.HORIZONTAL | SWT.BORDER)
  offset_slider.setMinimum(0)
	offset_slider.setMaximum(100000)
	offset_slider.setIncrement(1)
	offset_slider.setPageIncrement(1)
	offset_slider.setSelection(50000)
	offset_slider.addSelectionListener(sic_offset_sa)
  val offsetLabel = new Label(tools, SWT.NONE)
	offsetLabel.setText("OffsetSlider")
 
  val mod_slider = new Scale(tools,SWT.HORIZONTAL | SWT.BORDER)
  mod_slider.setMinimum(0)
	mod_slider.setMaximum(100000)
	mod_slider.setIncrement(1)
	mod_slider.setPageIncrement(1)
	mod_slider.setSelection(50000)
	mod_slider.addSelectionListener(sic_mod_sa)
  val modLabel = new Label(tools, SWT.NONE)
	modLabel.setText("TimeTranslationSlider")
 
  val prog_bar = new ProgressBar(tools,SWT.HORIZONTAL | SWT.BORDER)
  prog_bar.setMinimum(0)
  prog_bar.setMaximum(100)
  prog_bar.setSelection(0)
  prog_bar.setEnabled(false)
  
  val writeButton = new Button(tools, SWT.PUSH )
  writeButton.setText("Apply Time Drift")
  writeButton.addSelectionListener(new SelectionListener() {
      def widgetSelected(event : SelectionEvent) = {
        val savingprocess = new Runnable() {
          def run() = {
		        setTextEntry("set time drift\n")
		        (tex,destdir) match {
		          case (Some(p : COPI_Project), Some(d : String)) => {
		            var tl_counter = 0
		            for {
		              tl <- p.tls
		            } {
		              var counter = 0
		              for {
		                i <- tl.imgs
		              } {
		                counter += 1
		                ExifUtil.applyTimeToImage(tl,tl_counter.toString,i,tl.modTimeDiffSeconds, d)
		                setProgress(Some(counter.doubleValue()/tl.imgs.length))
		              }
		              setProgress(None)
		              tl_counter += 1
		            }
		          }
              case _ => setTextEntry("either no project scene node or else no dest directory set\n")
		        }
          }
        }
        new Thread(savingprocess).start
      }  
      
      def widgetDefaultSelected(event : SelectionEvent) = setTextEntry("set sth\n")
    })
 
  val textoutput = new Text(tools, SWT.MULTI | SWT.VERTICAL)
  textoutput.setSize(500,300)
  
  /************************* Layout ******************************/
  shell.setLayout(new FillLayout(SWT.VERTICAL))
  comp.setLayout(new FillLayout())
  tools.setLayout(new FillLayout(SWT.VERTICAL))
  
  /*
   * OpenGL-Rendering-Loop
   */
  def render = {
		GL11.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST)
		GL11.glEnable(GL11.GL_DEPTH_TEST)     
 

		  display.asyncExec(new Runnable() {
		    def run() = {
		      if (!canvas.isDisposed()) {
				canvas.setCurrent()
				try {
					GLContext.useContext(canvas)
				} catch {
					case e : LWJGLException => e.printStackTrace()
				}
		 
				progress match {
					case Some(v : Double) => {
						prog_bar.setSelection((v*100).toInt)
					}
					case _ => {
						prog_bar.setSelection(0)
					}
				}
    
				if (!textentry.isEmpty) {
				  val tuple = textentry.dequeue 
				  textoutput.append(tuple._1)
                  textentry = tuple._2
                }
		
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)
				GL11.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
				GL11.glLoadIdentity()
				GL11.glTranslated(0.0f, ytransl, -distance*100)
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)
		       
		        // minimize the framerate to a max of 50fps 
		        // to reduce cpu-burden
		        Thread.sleep(20)
		
				      // only render if there is a renderable scenegraph object defined
				tex match {
					case Some(renderable : COPI_BASE) => GLRenderer.render(renderable)
					case None =>
				}
			
				canvas.swapBuffers()
				display.asyncExec(this)
		      }
		    }
	})

    /* SWT Loop */
    //shell.pack() // auto resize
  	shell.open()

	  while (!shell.isDisposed()) {
	    if (!display.readAndDispatch())
	      display.sleep()
	  }
	  display.dispose()
  }
}
