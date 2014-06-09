package graphics.opengl;

import java.awt.Canvas;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import environment.Environment;
import environment.RandomFoodEnvironment;
import environment.generators.PerlinGenerator;
import graphics.Camera;

public class RenderGL {

	private Environment theEnvironment;
	private Camera camera;
	private int width, height;
	private double initHeight;

	public RenderGL(Canvas canvas, Environment env, int w, int h){
		// set up panel with respect to the evolution app
		theEnvironment = env;
		width = w;
		height = h;
		initHeight = (double) h;
		// initialize lwjgl display
		try {
			Display.setParent(canvas);
			Display.create();
		}
		catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// initialize opengl
		camera = new Camera();
		initGL();
	}

	public synchronized void redraw(){
		// in case screen size changed
		width = Display.getWidth();
		height = Display.getHeight();
		this.resizeWindow();
		// start drawing new frame
		camera.glSetView();
		clearGraphics();

		// update the display (i.e. swap buffers, etc)
		Display.update();
	}

	private void clearGraphics(){
		// clear screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
		// clear modelview matrix
		glLoadIdentity();
	}

	public Camera getCamera(){
		return camera;
	}

	private void initGL(){
		resizeWindow();
		// 2d, so save time by not depth-testing
		glDisable(GL_DEPTH_TEST);
		// set up line anti-aliasing
		glEnable(GL_LINE_SMOOTH);
		// allow standard alpha blending
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		// background clear color is black
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		// load and compile shaders
		initShaders();
		// necessary if using FBOs/textures
		glEnable(GL_TEXTURE_2D);
	}

	private void resizeWindow(){
		// no projection; set it to the identity matrix
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		double ar = (double) width / (double) height;
		glOrtho(- ar * initHeight/2,  ar * initHeight/2, -initHeight/2, initHeight/2, -1, 1);
		// set mode to modelview since this is where all drawing will be done
		glMatrixMode(GL_MODELVIEW);
		glViewport(0, 0, width, height);
	}

	private void initShaders(){
	}

	public void destroy(){
		// destroy lwjgl display
		Display.destroy();
	}

	public FloatBuffer screenToWorldCoordinates(int sx, int sy){
		IntBuffer viewport = BufferUtils.createIntBuffer(16);
		FloatBuffer modelview = BufferUtils.createFloatBuffer(16);
		FloatBuffer projection = BufferUtils.createFloatBuffer(16);
		FloatBuffer winZ = BufferUtils.createFloatBuffer(1);
		FloatBuffer position = BufferUtils.createFloatBuffer(3);
		glGetFloat( GL_MODELVIEW_MATRIX, modelview );
		glGetFloat( GL_PROJECTION_MATRIX, projection );
		glGetInteger( GL_VIEWPORT, viewport );
		glReadPixels(sx, sy, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, winZ);
		GLU.gluUnProject((float) sx, (float) sy, winZ.get(), modelview, projection, viewport, position);
		return position; 
	}
}
