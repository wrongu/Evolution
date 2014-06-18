package graphics.opengl;

import java.awt.Canvas;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

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
import static org.lwjgl.opengl.GL31.*;

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
	
	// allocate once
	private FloatBuffer mat4x4;
	
	// static vertex buffers (VBOs/meshes)
	private int screenquad_vbo, circle_vbo, kite_vbo;
	// Vertex Array Objects (VAOs)
	private int screenquad_vao, circle_vao, kite_vao;
	// uniform buffers (UBOs)
	private int organism_instance_ubo;
	private int ubo_circle_stride = 12, ubo_kite_stride = 16, ubo_binding = 0;
	
	// Shaders and programs
	private Program pPerlin, pOrgoCircle, pOrgoKite;
	private int perlin_lookup_tex;
	
	// other constants
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
		
		// draw environment background
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
		
		// calculate camera updates
		camera.projection(width, height).store(mat4x4);
		mat4x4.flip();
		
		// get all organisms to render (all that are within the camera's bounding box)
		LinkedList<AbstractOrganism> onscreen_organisms = theEnvironment.getInBox(camera.getWorldBounds((float)(width+2*SimpleCircleOrganism.DEFAULT_RANGE), (float)(height+2*SimpleCircleOrganism.DEFAULT_RANGE)));
	
		pOrgoKite.use();
		{
			glBindVertexArray(kite_vao);
			pOrgoKite.setUniformMat4("projection", mat4x4);
			// populate ubo with organism instance values
			glBindBuffer(GL_UNIFORM_BUFFER, organism_instance_ubo);
			FloatBuffer instance_data = ByteBuffer.allocateDirect(ubo_kite_stride).order(ByteOrder.nativeOrder()).asFloatBuffer();
			for(AbstractOrganism o : onscreen_organisms){
				SimpleCircleOrganism sco = (SimpleCircleOrganism) o;
				instance_data.put((float) o.getX());
				instance_data.put((float) o.getY());
				instance_data.put((float) sco.getVX());
				instance_data.put((float) sco.getVY());
				instance_data.flip();
				glBufferSubData(GL_UNIFORM_BUFFER, 0, instance_data);
				glDrawArrays(GL_TRIANGLES, 0, CIRCLE_DIVISIONS);
			}
		}
		pOrgoKite.unuse();
		
		pOrgoCircle.use();
		{
			glBindVertexArray(circle_vao);
			pOrgoCircle.setUniformMat4("projection", mat4x4);
			// populate ubo with organism instance values
			glBindBuffer(GL_UNIFORM_BUFFER, organism_instance_ubo);
			FloatBuffer instance_data = ByteBuffer.allocateDirect(ubo_circle_stride).order(ByteOrder.nativeOrder()).asFloatBuffer();
			for(AbstractOrganism o : onscreen_organisms){
				instance_data.put((float) o.getX());
				instance_data.put((float) o.getY());
				instance_data.put((float) o.getEnergy());
				instance_data.flip();
				glBufferSubData(GL_UNIFORM_BUFFER, 0, instance_data);
				glDrawArrays(GL_LINE_LOOP, 0, CIRCLE_DIVISIONS);
			}
		}
		pOrgoCircle.unuse();
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
		// initialize buffers
		initBuffers();
		// load and compile shaders
		initShaders();
		// create and populate textures
		initTextures();
		// initialize VAOs (must have shaders and buffers ready first)
		initVAOs();
		// necessary if using FBOs/textures
		glEnable(GL_TEXTURE_2D);
	}

	private void initBuffers() {
		// reserve space for buffers
		screenquad_vbo = glGenBuffers();
		circle_vbo = glGenBuffers();
		kite_vbo = glGenBuffers();
		organism_instance_ubo = glGenBuffers();
		
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
		
		////////////
		// CIRCLE //
		////////////
		
		FloatBuffer circle_mesh = BufferUtils.createFloatBuffer(2*CIRCLE_DIVISIONS);
		float w = (float) SimpleCircleOrganism.DEFAULT_RANGE / 2f;
		for(int i=0; i<CIRCLE_DIVISIONS; i++){
			double angle = i * 2d * Math.PI / CIRCLE_DIVISIONS;
			circle_mesh.put(w * (float) Math.cos(angle)); // x
			circle_mesh.put(w * (float) Math.sin(angle)); // y
		}
		circle_mesh.flip();
		glBindBuffer(GL_ARRAY_BUFFER, circle_vbo);
		glBufferData(GL_ARRAY_BUFFER, circle_mesh, GL_STATIC_DRAW);
	
		//////////
		// KITE //
		//////////
		
		float k = Config.instance.getFloat("KITE_SIZE");
		float t = Config.instance.getFloat("KITE_TAIL");
		FloatBuffer kite_mesh = BufferUtils.createFloatBuffer(18);
		kite_mesh.put(new float[]{
			 k,  0f, 0f,
			 0f, k,  0f,
			-k,  0f, t,
			-k,  0f, t,
			 0f,-k,  0f,
			 k,  0f, 0f
		});
		kite_mesh.flip();
		glBindBuffer(GL_ARRAY_BUFFER, kite_vbo);
		glBufferData(GL_ARRAY_BUFFER, kite_mesh, GL_STATIC_DRAW);
		
		///////////////////
		// ORGO INSTANCE //
		///////////////////
		
		glBindBuffer(GL_UNIFORM_BUFFER, organism_instance_ubo);
		// glBufferData allocates space, so that later we can repopulate with glBufferSubData
		glBufferData(GL_UNIFORM_BUFFER, Math.max(ubo_circle_stride, ubo_kite_stride), GL_STREAM_DRAW);
	}

	private void initShaders(){
		pPerlin = Program.createProgram("shaders/vert_screenToWorld.glsl", "shaders/frag_perlin.glsl");
		// set uniforms (setting here rather than in redraw() since they won't change)
		pPerlin.use();
		{
			RandomFoodEnvironment rfe = (RandomFoodEnvironment) theEnvironment;
			PerlinGenerator pg = (PerlinGenerator) rfe.getGenerator();
			pPerlin.setUniformi("octaves", pg.getOctaves());
			pPerlin.setUniformf("t_size",  (float) PerlinGenerator.TABLE_SIZE);
			pPerlin.setUniformf("scale", (float) pg.getScale());
			pPerlin.setUniformi("table", 0); // using GL_TEXTURE0
		}
		pPerlin.unuse();
		
		pOrgoCircle = Program.createProgram("shaders/vert_organism.glsl", "shaders/frag_energyCircle.glsl");
		pOrgoKite   = Program.createProgram("shaders/vert_kite.glsl", "shaders/frag_kite.glsl");
		exitOnGLError("Shader compilation");
	}
	
	private void initTextures(){
		RandomFoodEnvironment rfe = (RandomFoodEnvironment) theEnvironment;
		PerlinGenerator pg = (PerlinGenerator) rfe.getGenerator();
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

	private void initVAOs() {
		// reserve space for VAOs
		screenquad_vao = glGenVertexArrays();
		circle_vao = glGenVertexArrays();
		kite_vao = glGenVertexArrays();
		
		/////////////////////////
		// SCREENQUAD BINDINGS //
		/////////////////////////
		
		glBindVertexArray(screenquad_vao);
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, screenquad_vbo);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);
		
		/////////////////////
		// CIRCLE BINDINGS //
		/////////////////////
		
		glBindVertexArray(circle_vao);
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, circle_vbo);
		
		int attrLocVertex = pOrgoCircle.getAttribute("vertex");
		glVertexAttribPointer(attrLocVertex, 2, GL_FLOAT, false, 8, 0);
		
		pOrgoCircle.bindUniformBlock("instanceBlock", organism_instance_ubo, ubo_binding);
		glBindBufferBase(GL_UNIFORM_BUFFER, ubo_binding, organism_instance_ubo);
		
		///////////////////
		// KITE BINDINGS //
		///////////////////
		
		glBindVertexArray(kite_vao);
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, kite_vbo);
		int attrLocVertex2 = pOrgoKite.getAttribute("vertex");
		glVertexAttribPointer(attrLocVertex2, 3, GL_FLOAT, false, 12, 0);
		
		pOrgoKite.bindUniformBlock("instanceBlock", organism_instance_ubo, ubo_binding);
		glBindBufferBase(GL_UNIFORM_BUFFER, ubo_binding, organism_instance_ubo);
	}

	public void destroy(){
		// clean up opengl state
		if(screenquad_vao != 0) glDeleteVertexArrays(screenquad_vao);
		if(screenquad_vbo != 0) glDeleteBuffers(screenquad_vbo);
		if(circle_vao != 0) glDeleteVertexArrays(circle_vao);
		if(circle_vbo != 0) glDeleteBuffers(circle_vbo);
		if(kite_vao != 0) glDeleteVertexArrays(kite_vao);
		if(kite_vbo != 0) glDeleteBuffers(kite_vbo);
		if(organism_instance_ubo != 0) glDeleteBuffers(organism_instance_ubo);
		if(perlin_lookup_tex != 0) glDeleteTextures(perlin_lookup_tex);
		if(pPerlin != null) pPerlin.destroy();
		if(pOrgoCircle != null) pOrgoCircle.destroy();
		if(pOrgoKite != null) pOrgoKite.destroy();
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
