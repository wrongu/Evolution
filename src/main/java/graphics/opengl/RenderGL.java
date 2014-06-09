package graphics.opengl;

import java.awt.Canvas;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.BufferUtils;

import applet.Config;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import environment.Environment;
import graphics.Camera;

public class RenderGL {

	private Environment theEnvironment;
	private Camera camera;
	private int width, height;
	private float aspect;
	private boolean[] keyboard;
	private int[] mouse_buttons;
	private double camera_sensitivity;
	boolean fbo_enabled;
	
	// specific buffers
	int screenquad_vbo, screenquad_vao;
	
	// debugging
	Program pDebug;

	public RenderGL(Canvas canvas, Environment env, int w, int h){
		// set up panel with respect to the evolution app
		theEnvironment = env;
		width = w;
		height = h;
		aspect = (float) w / (float) h;
		camera_sensitivity = Config.instance.getDouble("CAMERA_SENSETIVITY");
		// initialize lwjgl display
		try {
			Display.setParent(canvas);
			Display.setVSyncEnabled(true);
			Display.setTitle("Evolution Sim");
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
		clearAll();
		// in case screen size changed
		width = Display.getWidth();
		height = Display.getHeight();
		updateAspectRatio();
		// start drawing new frame
		//camera.glSetView();
		pDebug.use();
		{
			glBindVertexArray(screenquad_vao);
			glDrawArrays(GL_TRIANGLES, 0, 3);
		}
		pDebug.unuse();
		// update the display (i.e. swap buffers, etc)
		Display.update();
	}

	private void clearAll(){
		// clear screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
	}

	public void moveCamera() {
		double dx = 0.0, dy = 0.0;
		if(keyboard[0]) dy -= camera_sensitivity;
		if(keyboard[1]) dy += camera_sensitivity;
		if(keyboard[2]) dx += camera_sensitivity;
		if(keyboard[3]) dx -= camera_sensitivity;
		camera.shift(dx, dy);
		camera.zoom((double) mouse_buttons[1] * 0.0005 * camera_sensitivity);
	}

	private void initGL(){
		
		updateAspectRatio();
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
		initGLShaders();
		// initialize GL buffers
		initGLBuffers();
		// necessary if using FBOs/textures
		glEnable(GL_TEXTURE_2D);
	}

	private void updateAspectRatio(){
//		// no projection; set it to the identity matrix
//		glMatrixMode(GL_PROJECTION);
//		glLoadIdentity();
//		double ar = (double) width / (double) height;
//		glOrtho(- ar * initHeight/2,  ar * initHeight/2, -initHeight/2, initHeight/2, -1, 1);
//		// set mode to modelview since this is where all drawing will be done
//		glMatrixMode(GL_MODELVIEW);
		glViewport(0, 0, width, height);
	}

	private void initGLShaders(){
		fbo_enabled = GLContext.getCapabilities().GL_EXT_framebuffer_object;
		if(fbo_enabled){
			Shader vNoop = Shader.fromSource("shaders/noop.vert", GL_VERTEX_SHADER);
			Shader fDebug = Shader.fromSource("shaders/debug.frag", GL_FRAGMENT_SHADER);
			pDebug = Program.createProgram(vNoop, fDebug);
			exitOnGLError("Shader compilation");
		} else{
			System.err.println("FBO not available");
		}
	}

	private void initGLBuffers() {
		screenquad_vbo = glGenBuffers();
		FloatBuffer triangle = BufferUtils.createFloatBuffer(9);
		triangle.put(new float[]{
				 0.0f,  0.5f,  0.0f,
				-0.5f, -0.5f,  0.0f,
				 0.5f, -0.5f,  0.0f
		});
		triangle.flip();
//		screen_corners.put(-1f); screen_corners.put(-1f);
//		screen_corners.put( 1f); screen_corners.put(-1f);
//		screen_corners.put(-1f); screen_corners.put( 1f);
//		screen_corners.put(-1f); screen_corners.put( 1f);
//		screen_corners.put( 1f); screen_corners.put(-1f);
//		screen_corners.put( 1f); screen_corners.put( 1f);
		glBindBuffer(GL_ARRAY_BUFFER, screenquad_vbo);
		glBufferData(GL_ARRAY_BUFFER, triangle, GL_STATIC_DRAW);
		
		screenquad_vao = glGenVertexArrays();
		glBindVertexArray(screenquad_vao);
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, screenquad_vbo);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0);
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
	
	public void bindInputs(boolean[] keyboard, int[] mouse){
		this.keyboard = keyboard;
		this.mouse_buttons = mouse;
	}
	
	private void exitOnGLError(String errorMessage) {
		int errorValue = GL11.glGetError();
		
		if (errorValue != GL11.GL_NO_ERROR) {
			String errorString = GLU.gluErrorString(errorValue);
			System.err.println("ERROR - " + errorMessage + ": " + errorString);
			
			if (Display.isCreated()) Display.destroy();
			System.exit(-1);
		}
	}
}
