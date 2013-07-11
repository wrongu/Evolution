package graphics;

import java.awt.Canvas;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import static org.lwjgl.opengl.GL11.*;

import environment.Environment;

public class RenderPanelGL extends JPanel {

	private static final long serialVersionUID = -5610531621479479038L;

	private Environment theEnvironment;
	private int glDisplayList;
	private Camera camera;
	
	public RenderPanelGL(Environment env, int w, int h){
		// set up panel with respect to the evolution app
		theEnvironment = env;
		// set up canvas for lwjgl and opengl
		Canvas canv = new Canvas();
		canv.setPreferredSize(new Dimension(w,h));
		this.add(canv);
		// initialize lwjgl display
		try {
			Display.setParent(canv);
			Display.create();
		}
		catch (LWJGLException e) {
			e.printStackTrace();
		}
		// initialize opengl
		camera = new Camera(0, 0, w, h);
		initGL();

		// set the graphics update busy-loop thread running
		Thread updater = new UpdateThread();
		updater.setDaemon(true);
		updater.start();
	}
	
	public synchronized void redraw(){
		// clear screen
		glClear(GL_COLOR_BUFFER_BIT);
		// clear modelview matrix
		glLoadIdentity();
		// move camera (by setting the bounding box of opengl's rendering in glOrtho())
		camera.glSetView();
		// start list compilation and write all draw() operations to that list
		glNewList(glDisplayList, GL_COMPILE);
		{
			theEnvironment.draw();
		}
		glEndList();
		// list is finished; calling it means render everything at once
		glCallList(glDisplayList);
	}
	
	private void initGL(){
		// 2d, so save time by not depth-testing
		glDisable(GL_DEPTH_TEST);
		// set up line antialiasing
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_LINE_SMOOTH);
		// background clear color is black
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		// tiny orthographic init. real ortho projection is updated in Camera.glSetView()
		glOrtho(0.,0.,1.,1.,.3,1.);
		// no projection; set it to the identity matrix
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		// set mode to modelview since this is where all drawing will be done
		glMatrixMode(GL_MODELVIEW);
		// opengl works fastest when it has compilation lists to work from. note that in redraw(), we set up the list to compile,
		//	then do all drawing (which really just fills the list with commands), then do glCallList, which executes all drawing
		// 	at once and lets opengl do all its own optimizations.
		glDisplayList = glGenLists(1);
	}
	
	private class UpdateThread extends Thread{
		@Override
		public void run(){
			while(true){
				redraw();
			}
		}
	}
}
