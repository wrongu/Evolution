package graphics.opengl;

import java.awt.Canvas;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import environment.Environment;
import environment.RandomFoodEnvironment;
import environment.generators.PerlinGenerator;
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
	
	// allocate once
	FloatBuffer mProjInv;
	
	// specific buffers
	int screenquad_vbo, screenquad_vao;
	int perlin_lookup_tex;
	
	// debugging
	Program pPerlin;

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
		mProjInv = BufferUtils.createFloatBuffer(16);
		initGL();
	}

	public synchronized void redraw(){
		clearAll();
		camera.ease();
		// in case screen size changed
		width = Display.getWidth();
		height = Display.getHeight();
		glViewport(0, 0, width, height);
		// start drawing new frame
		pPerlin.use();
		{
			camera.inverse_projection(width, height).store(mProjInv);
			mProjInv.flip();
			pPerlin.setUniformMat4("inverse_projection", mProjInv);
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_1D, perlin_lookup_tex);
			glDrawArrays(GL_TRIANGLES, 0, 6);
		}
		pPerlin.unuse();
		// update the display (i.e. swap buffers, etc)
		Display.update();
	}

	private void clearAll(){
		// clear screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
	}

	public void moveCamera() {
		double dx = 0.0, dy = 0.0;
		if(keyboard[0]) dy += camera_sensitivity;
		if(keyboard[1]) dy -= camera_sensitivity;
		if(keyboard[2]) dx -= camera_sensitivity;
		if(keyboard[3]) dx += camera_sensitivity;
		camera.shift(dx, dy);
		camera.zoom((double) mouse_buttons[1] * 0.005 * camera_sensitivity);
	}

	private void initGL(){

		glViewport(0, 0, width, height);
		// 2d, so save time by not depth-testing
		glDisable(GL_DEPTH_TEST);
		// set up line anti-aliasing
		glEnable(GL_LINE_SMOOTH);
		// allow standard alpha blending
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		// background clear color is black
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		// initialize GL buffers
		initGLBuffers();
		// load and compile shaders
		initGLShaders();
		// necessary if using FBOs/textures
		glEnable(GL_TEXTURE_2D);
	}

	private void initGLShaders(){
		fbo_enabled = GLContext.getCapabilities().GL_EXT_framebuffer_object;
		if(fbo_enabled){
			Shader vNoop = Shader.fromSource("shaders/screenToWorld.vert", GL_VERTEX_SHADER);
			Shader fPerlin = Shader.fromSource("shaders/perlin.frag", GL_FRAGMENT_SHADER);
			pPerlin = Program.createProgram(vNoop, fPerlin);
			// set uniforms
			pPerlin.use();
			{
				RandomFoodEnvironment rfe = (RandomFoodEnvironment) theEnvironment;
				PerlinGenerator pg = (PerlinGenerator) rfe.getGenerator();
				pPerlin.setUniformi("octaves", pg.getOctaves());
				pPerlin.setUniformf("t_size",  (float) PerlinGenerator.TABLE_SIZE);
				pPerlin.setUniformf("scale", (float) pg.getScale());
				pPerlin.setUniformi("table", 0); // using GL_TEXTURE0
				FloatBuffer table = BufferUtils.createFloatBuffer(PerlinGenerator.TABLE_SIZE);
				table.put(pg.getTableNormalized()); table.flip();
				// create perlin lookup texture
				perlin_lookup_tex = glGenTextures();
				glEnable(GL_TEXTURE_1D); // TODO test if this can be removed
				glActiveTexture(GL_TEXTURE0);
				glBindTexture(GL_TEXTURE_1D, perlin_lookup_tex);
				// the use of GL_RED here is basically saying that there is only 1 channel of data (as opposed to RGB which has 3)
				glTexImage1D(GL_TEXTURE_1D, 0, GL_R32F, PerlinGenerator.TABLE_SIZE, 0, GL11.GL_RED, GL_FLOAT, table);
				glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
				glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
				glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_REPEAT);
				glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_WRAP_T, GL_REPEAT);
			}
			pPerlin.unuse();
			exitOnGLError("Shader compilation");
		} else{
			System.err.println("FBO not available");
		}
	}

	private void initGLBuffers() {
		screenquad_vbo = glGenBuffers();
		FloatBuffer screen_corners = BufferUtils.createFloatBuffer(12);
		screen_corners.put(new float[]{
				-1.0f, -1.0f,
				 1.0f, -1.0f,
				-1.0f,  1.0f,
				-1.0f,  1.0f,
				 1.0f, -1.0f,
				 1.0f,  1.0f
		});
		screen_corners.flip();
		glBindBuffer(GL_ARRAY_BUFFER, screenquad_vbo);
		glBufferData(GL_ARRAY_BUFFER, screen_corners, GL_STATIC_DRAW);
		
		screenquad_vao = glGenVertexArrays();
		glBindVertexArray(screenquad_vao);
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, screenquad_vbo);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);
	}

	public void destroy(){
		// clean up opengl state
		if(screenquad_vao != 0) glDeleteVertexArrays(screenquad_vao);
		if(screenquad_vbo != 0) glDeleteBuffers(screenquad_vbo);
		if(perlin_lookup_tex != 0) glDeleteTextures(perlin_lookup_tex);
		if(pPerlin != null) pPerlin.destroy();
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
