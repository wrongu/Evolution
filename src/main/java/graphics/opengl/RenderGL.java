package graphics.opengl;

import java.awt.Canvas;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.BufferUtils;

import bio.organisms.AbstractOrganism;
import bio.organisms.SimpleCircleOrganism;

import applet.Config;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.ARBInstancedArrays.*;
import static org.lwjgl.opengl.ARBDrawInstanced.*;

import environment.Environment;
import environment.RandomFoodEnvironment;
import environment.generators.PerlinGenerator;
import graphics.Camera;

public class RenderGL {

	private Environment theEnvironment;
	private Camera camera;
	private int width, height;
	private boolean[] keyboard;
	private int[] mouse_buttons;
	private double camera_sensitivity;
	boolean fbo_enabled;
	
	// allocate once
	private FloatBuffer mat4x4;
	
	// specific buffers
	private int screenquad_vbo, screenquad_vao;
	private int circle_vbo, organism_instances_vbo, organisms_vao, kite_vbo, kite_vao;
	private int n_instance_attributes = 5; // x, y, energy, speed, direction
	
	// Shaders and programs
	private Program pPerlin, pOrgoCircles, pOrgoKites;
	private int perlin_lookup_tex;
	
	// drawin circles
	private final int CIRCLE_DIVISIONS = Config.instance.getInt("CIRCLE_SUBDIVISIONS");

	public RenderGL(Canvas canvas, Environment env, int w, int h){
		// set up panel with respect to the evolution app
		theEnvironment = env;
		width = w;
		height = h;
		camera_sensitivity = Config.instance.getDouble("CAMERA_SENSETIVITY");
		// initialize lwjgl display
		try {
			Display.setParent(canvas);
			Display.setVSyncEnabled(true);
			Display.setTitle("Evolution Sim");
			ContextAttribs contextAtrributes = new ContextAttribs(3, 2).withForwardCompatible(true).withProfileCore(true);
			PixelFormat pf = new PixelFormat();
			Display.create(pf, contextAtrributes);
			exitOnGLError("context setup");
		}
		catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// initialize opengl
		camera = new Camera();
		mat4x4 = BufferUtils.createFloatBuffer(16);
		initGL();
	}

	public synchronized void redraw(){
		clearAll();
		camera.ease();
		
		// in case screen size changed
		width = Display.getWidth();
		height = Display.getHeight();
		glViewport(0, 0, width, height);
		
		// draw environment
		pPerlin.use();
		{
			glBindVertexArray(screenquad_vao);
			camera.inverse_projection(width, height).store(mat4x4);
			mat4x4.flip();
			pPerlin.setUniformMat4("inverse_projection", mat4x4);
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_1D, perlin_lookup_tex);
			glDrawArrays(GL_TRIANGLES, 0, 6);
		}
		pPerlin.unuse();
		
		// prepare projection matrix
		camera.projection(width, height).store(mat4x4);
		mat4x4.flip();
		
		// create organisms' instance data
		LinkedList<AbstractOrganism> onscreen_organisms = theEnvironment.getInBox(camera.getWorldBounds((float)(width+2*SimpleCircleOrganism.DEFAULT_RANGE), (float)(height+2*SimpleCircleOrganism.DEFAULT_RANGE)));
		int n_organisms = onscreen_organisms.size();
		FloatBuffer attribute_buffer = BufferUtils.createFloatBuffer(n_instance_attributes * n_organisms);
		for(AbstractOrganism o : onscreen_organisms){
			SimpleCircleOrganism sco = (SimpleCircleOrganism) o;
			attribute_buffer.put((float) o.getX());
			attribute_buffer.put((float) o.getY());
			attribute_buffer.put((float) o.getEnergy());
			attribute_buffer.put((float) sco.getSpeed());
			attribute_buffer.put((float) sco.getDirection());
		}
		attribute_buffer.flip();
		
		// send instance data to GPU
		glBindBuffer(GL_ARRAY_BUFFER, organism_instances_vbo);
		glBufferData(GL_ARRAY_BUFFER, attribute_buffer, GL_DYNAMIC_DRAW);
		
		// Draw Kites
		pOrgoKites.use();
		{
			glBindVertexArray(kite_vao);
			pOrgoKites.setUniformMat4("projection", mat4x4);
			glDrawArraysInstancedARB(GL_TRIANGLES, 0, 6, n_organisms);
		}
		pOrgoKites.unuse();
		
		// Draw Circles
		pOrgoCircles.use();
		{
			glBindVertexArray(organisms_vao);
			pOrgoCircles.setUniformMat4("projection", mat4x4);
			glDrawArraysInstancedARB(GL_LINE_LOOP, 0, CIRCLE_DIVISIONS, n_organisms);
		}
		pOrgoCircles.unuse();
		
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
		// initialize vbo buffers
		initVBOs();
		// load and compile shaders
		initShaders();
		// initialize vaos (requiers shaders and vbos done first)
		initVAOs();
		// necessary if using FBOs/textures
		glEnable(GL_TEXTURE_2D);
	}

	private void initVBOs() {
		// make some buffers
		screenquad_vbo = glGenBuffers();
		circle_vbo = glGenBuffers();
		organism_instances_vbo = glGenBuffers();
		kite_vbo = glGenBuffers();
		
		/////////////////
		// SCREEN-QUAD //
		/////////////////
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

		//////////////
		// KITE VBO //
		//////////////
		FloatBuffer kite_points = BufferUtils.createFloatBuffer(18);
		float kite_size = Config.instance.getFloat("KITE_SIZE");
		kite_points.put(new float[]{
				// x, y, scale-with-speed
				 kite_size,  0f,  0f,
				 0f,  kite_size,  0f,
				-kite_size,  0f, 2f/kite_size, // nonzero in 3rd column means this 
				-kite_size,  0f, 2f/kite_size, //  vertex will scale with speed
				 0f, -kite_size,  0f,
				 kite_size,  0f,  0f
		});
		kite_points.flip();
		glBindBuffer(GL_ARRAY_BUFFER, kite_vbo);
		glBufferData(GL_ARRAY_BUFFER, kite_points, GL_STATIC_DRAW);
		
		////////////////
		// CIRCLE VBO //
		////////////////
		FloatBuffer circle_points = BufferUtils.createFloatBuffer(2*CIRCLE_DIVISIONS);
		float w = (float) SimpleCircleOrganism.DEFAULT_RANGE / 2f;
		for(int i=0; i<CIRCLE_DIVISIONS; i++){
			double angle = i * 2d * Math.PI / (double) CIRCLE_DIVISIONS;
			circle_points.put(w * (float) Math.cos(angle)); // x
			circle_points.put(w * (float) Math.sin(angle)); // y
		}
		circle_points.flip();
		glBindBuffer(GL_ARRAY_BUFFER, circle_vbo);
		glBufferData(GL_ARRAY_BUFFER, circle_points, GL_STATIC_DRAW);
		
		exitOnGLError("initVBOs");
	}

	private void initShaders(){
		pPerlin = Program.createProgram("shaders/screenToWorld.vert", "shaders/perlin.frag");
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
		
		pOrgoCircles = Program.createProgram("shaders/organism.vert", "shaders/organism.frag");
		
		pOrgoKites = Program.createProgram("shaders/kitespeed.vert", "shaders/debug.frag");
	
		exitOnGLError("initShaders");
	}

	private void initVAOs() {
		// make some vertex arrays
		screenquad_vao = glGenVertexArrays();
		organisms_vao = glGenVertexArrays();
		kite_vao = glGenVertexArrays();
		
		////////////////
		// SCREENQUAD //
		////////////////
		
		glBindVertexArray(screenquad_vao);
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, screenquad_vbo);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);
		
		// stride for all attributes sharing the organism_instances_vbo
		int stride = 4 * n_instance_attributes; // 4 is the number of bytes in a float
		
		/////////////////////
		// ORGANISMS/KITES //
		/////////////////////

		int attrLocVertex2   = pOrgoKites.getAttribute("vertex");
		int attrLocPosition2 = pOrgoKites.getAttribute("position");
		int attrLocSpeed     = pOrgoKites.getAttribute("speed");
		int attrLocDirection = pOrgoKites.getAttribute("direction");
		
		glBindVertexArray(kite_vao);
		// static kite attribute
		glBindBuffer(GL_ARRAY_BUFFER, kite_vbo);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(attrLocVertex2, 3, GL_FLOAT, false, 12, 0);
		// instanced organisms attributes
		glBindBuffer(GL_ARRAY_BUFFER, organism_instances_vbo);
		// set up instanced position attribute
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(attrLocPosition2, 2, GL_FLOAT, false, stride, 0);
		glVertexAttribDivisorARB(attrLocPosition2, 1);
		// set up instanced speed attribute
		glEnableVertexAttribArray(2);
		glVertexAttribPointer(attrLocSpeed, 1, GL_FLOAT, false, stride, 12);
		glVertexAttribDivisorARB(attrLocSpeed, 1);
		// set up instanced direction attribute
		glEnableVertexAttribArray(3);
		glVertexAttribPointer(attrLocDirection, 1, GL_FLOAT, false, stride, 16);
		glVertexAttribDivisorARB(attrLocDirection, 1);
		
		///////////////////////
		// ORGANISMS/CIRCLES //
		///////////////////////
		
		int attrLocVertex1   = pOrgoCircles.getAttribute("vertex");
		int attrLocPosition1 = pOrgoCircles.getAttribute("position");
		int attrLocEnergy   = pOrgoCircles.getAttribute("energy");
		
		glBindVertexArray(organisms_vao);
		// static circle attribute
		glBindBuffer(GL_ARRAY_BUFFER, circle_vbo);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(attrLocVertex1, 2, GL_FLOAT, false, 8, 0);
		// instanced organisms attributes
		glBindBuffer(GL_ARRAY_BUFFER, organism_instances_vbo);
		// set up instanced position attribute
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(attrLocPosition1, 2, GL_FLOAT, false, stride, 0);
		glVertexAttribDivisorARB(attrLocPosition1, 1);
		// set up instanced energy attribute (note offset of 8 since x and y take up 4 bytes each)
		glEnableVertexAttribArray(2);
		glVertexAttribPointer(attrLocEnergy, 1, GL_FLOAT, false, stride, 8);
		glVertexAttribDivisorARB(attrLocEnergy, 1);
		
		exitOnGLError("initVAOs");
	}

	public void destroy(){
		// clean up opengl state
		if(screenquad_vbo != 0) glDeleteBuffers(screenquad_vbo);
		if(organism_instances_vbo != 0) glDeleteBuffers(organism_instances_vbo);
		if(circle_vbo != 0) glDeleteBuffers(circle_vbo);
		if(screenquad_vao != 0) glDeleteVertexArrays(screenquad_vao);
		if(organisms_vao != 0) glDeleteVertexArrays(organisms_vao);
		if(perlin_lookup_tex != 0) glDeleteTextures(perlin_lookup_tex);
		if(pPerlin != null) pPerlin.destroy();
		// destroy lwjgl display
		Display.destroy();
	}
	
	private void modelMatrix(float tx, float ty, float rz, FloatBuffer dest){
		Matrix4f mat = new Matrix4f();
		Matrix4f.setIdentity(mat);
		float c = (float) Math.cos(rz);
		float s = (float) Math.sin(rz);
		mat.m00 = c;
		mat.m01 = s;
		mat.m10 = -s;
		mat.m11 = c;
		mat.m30 = tx;
		mat.m31 = ty;
		mat.store(dest);
		dest.flip();
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
