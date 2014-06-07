package graphics.opengl;

import java.awt.Canvas;
import java.nio.ByteBuffer;
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
	private boolean[] keyboard;
	private int[] mouse_buttons;

	// opengl is a state machine that gives us references to its objects as ints.
	private int drawList; // list mode

	// TODO - implement checks for FBOEnabled
	private static boolean FBOEnabled; 
	private Shader vNoop, fPerlin;
	private Program pPerlin;
	private int perlin_table_tex;
	private PerlinGenerator gen;

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
		}
		// initialize opengl
		camera = new Camera();
		initGL();
	}

	public synchronized void redraw(){
		// in case screen size changed
		width = Display.getWidth();
		height = Display.getHeight();
		glWindowSize();
		// start list compilation and write all draw() operations to that list
		glNewList(drawList, GL_COMPILE);
		{
			// move camera (i.e. do glTranslate to move the objects on the screen, as if the camera were moving)
			camera.glSetView();
			if(pPerlin != null){
				pPerlin.begin();
				{
					this.renderFullScreenQuadInWorld();
				}
				pPerlin.end();
			}
			theEnvironment.glDraw();
		}
		glEndList();

		/* 
		 * At this point, all drawing is queued up on lists. 
		 * all that's left to render them is a call to glCallList
		 */
		
		{
			clearGraphics();
			glCallList(drawList);
		}

		// update the display (i.e. swap buffers, etc)
		Display.update();
	}

	private void renderFullScreenQuadInWorld(){
		glColor3f(0,1,1);
		FloatBuffer bottomLeft = screenToWorldCoordinates(0, 0);
		FloatBuffer topRight = screenToWorldCoordinates(width, height);
		float xlo = bottomLeft.get();
		float ylo = bottomLeft.get();
		float xhi = topRight.get();
		float yhi = topRight.get();
		glBegin(GL_QUADS);
		{
			glVertex2f(xlo, ylo);
			glVertex2f(xhi, ylo);
			glVertex2f(xhi, yhi);
			glVertex2f(xlo, yhi);
		}
		glEnd();
	}

	private void clearGraphics(){
		// clear screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
		// clear modelview matrix
		glLoadIdentity();
	}

	public void moveCamera(double dt) {
		double dx = 0.0, dy = 0.0;
		if(keyboard[0]) dy -= .1*dt;
		if(keyboard[1]) dy += .1*dt;
		if(keyboard[2]) dx += .1*dt;
		if(keyboard[3]) dx -= .1*dt;
		camera.shift(dx, dy);
		camera.zoom((double) mouse_buttons[1] * 0.0005);
	}

	private void initGL(){
		glWindowSize();
		// opengl works fastest when it has compilation lists to work from. note that in redraw(), we set up the list to compile,
		//	then do all drawing (which really just fills the list with commands), then do glCallList, which executes all drawing
		// 	at once and lets opengl do all its own optimizations.
		drawList = glGenLists(1);
		// 2d, so save time by not depth-testing
		glDisable(GL_DEPTH_TEST);
		// set up line antialiasing
		glEnable(GL_LINE_SMOOTH);
		// allow standard alpha blending
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		// background clear color is black
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		// load and compile shaders
		initGLShaders();
		glEnable(GL_TEXTURE_2D);
	}

	private void glWindowSize(){
		// no projection; set it to the identity matrix
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		double ar = (double) width / (double) height;
		glOrtho(- ar * initHeight/2,  ar * initHeight/2, -initHeight/2, initHeight/2, -1, 1);
		// set mode to modelview since this is where all drawing will be done
		glMatrixMode(GL_MODELVIEW);
		glViewport(0, 0, width, height);
	}

	private void initGLShaders(){
		FBOEnabled = GLContext.getCapabilities().GL_EXT_framebuffer_object;
		if(FBOEnabled){
			// create shaders and their associated program
			{
				vNoop = Shader.fromSource("shaders/shader.vert", GL_VERTEX_SHADER);

				if(theEnvironment instanceof RandomFoodEnvironment){
					if(((RandomFoodEnvironment) theEnvironment).getGenerator() instanceof PerlinGenerator){
						gen = (PerlinGenerator) ((RandomFoodEnvironment) theEnvironment).getGenerator();
						fPerlin = Shader.fromSource("shaders/perlin.frag", GL_FRAGMENT_SHADER);
						
						// create programs from the shaders
//						pPerlin = Program.createProgram(vNoop, fPerlin);
//						pPerlin.begin();
//						{
//							pPerlin.setUniformi("octaves", gen.getOctaves());
//							pPerlin.setUniformi("t_size", PerlinGenerator.TABLE_SIZE);
//							pPerlin.setUniformf("scale", (float) gen.getScale());
//							pPerlin.setUniformi("table", 0); // bind to TEXTURE0
//						}
//						pPerlin.end();
						
						// create texture for perlin table
						perlin_table_tex = glGenTextures();
						glActiveTexture(GL_TEXTURE0);
						glBindTexture(GL_TEXTURE_1D, perlin_table_tex);
						IntBuffer tablebuffer = ByteBuffer.allocateDirect(PerlinGenerator.TABLE_SIZE*Integer.SIZE).asIntBuffer();
						tablebuffer.put(gen.getTable(), 0, PerlinGenerator.TABLE_SIZE);
						glTexImage1D(GL_TEXTURE_1D, 0, GL_ALPHA16, PerlinGenerator.TABLE_SIZE, 0, GL_RED, GL_UNSIGNED_INT, tablebuffer);
					}
				}
			}
		} else{
			System.err.println("FBO not available");
		}
	}

	public void destroy(){
		// destroy shaders
		if(vNoop != null)  vNoop.destroy();
		if(fPerlin != null) fPerlin.destroy();
		// destroy programs
		if(pPerlin != null) pPerlin.destroy();
		// destroy textures
		if(perlin_table_tex != 0) glDeleteTextures(perlin_table_tex);
		// destroy lwjgl display
		Display.destroy();
	}

	public void bindInputs(boolean[] direction_keys, int[] mouse_move, int[] mouse_buttons) {
		keyboard = direction_keys;
		this.mouse_buttons = mouse_buttons;
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
