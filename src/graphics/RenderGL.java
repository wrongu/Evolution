package graphics;

import java.awt.Canvas;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import static org.lwjgl.opengl.GL11.*;

import environment.Environment;

public class RenderGL {

	private static final long serialVersionUID = -5610531621479479038L;

	private Environment theEnvironment;
	private int glDisplayList;
	private Camera camera;
	private int width, height;

	public RenderGL(Canvas canvas, Environment env, int w, int h){
		// set up panel with respect to the evolution app
		theEnvironment = env;
		width = w;
		height = h;

		// initialize lwjgl display
		try {
			Display.setParent(canvas);
			Display.create();
		}
		catch (LWJGLException e) {
			e.printStackTrace();
		}
		// initialize opengl
		camera = new Camera();
		initGL();
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
			theEnvironment.glDraw();
		}
		glEndList();
		// list is finished; calling it means render everything at once
		glCallList(glDisplayList);
		// update the lwjgl display with the current opengl frame
		Display.update();
	}

	private void initGL(){
		// no projection; set it to the identity matrix
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, width, 0, height, -1, 1);
		// set mode to modelview since this is where all drawing will be done
		glMatrixMode(GL_MODELVIEW);
		// opengl works fastest when it has compilation lists to work from. note that in redraw(), we set up the list to compile,
		//	then do all drawing (which really just fills the list with commands), then do glCallList, which executes all drawing
		// 	at once and lets opengl do all its own optimizations.
		glDisplayList = glGenLists(1);
		// 2d, so save time by not depth-testing
		glDisable(GL_DEPTH_TEST);
		// set up line antialiasing
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_LINE_SMOOTH);
		// background clear color is black
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
	}
}
