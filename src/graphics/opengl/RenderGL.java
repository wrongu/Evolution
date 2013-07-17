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
import static org.lwjgl.opengl.GL20.*;


import environment.Environment;
import graphics.Camera;

public class RenderGL {

	private static final long serialVersionUID = -5610531621479479038L;

	private Environment theEnvironment;
	private Camera camera;
	private int width, height;
	private double initHeight;
	private boolean[] keyboard;
	private int[] mouse_buttons;

	private GLList glOrganismList;
	private GLList glBackgroundList;

	// the following opengl references are all for the glow effect. Requires FBO to be enabled.
	// TODO - implement checks for FBOEnabled
	private static boolean FBOEnabled; 
	// vertex shaders
	private Shader vDefault, vScreenTex;
	// fragment shaders
	private Shader fBlur, fBlend, fDefault;
	private Program pScreenTex, pHBlur, pVBlur, pBlend;
	private FrameBuffer glEffects;

	private static final int GLOW_MODE = 0; // additive - high overexposure effect
	//	private static final int GLOW_MODE = 1; // screen blending - medium effect
	//	private static final int GLOW_MODE = 2; // soft lighting - no overexposure
	//	private static final int GLOW_MODE = 3; // show raw glow texture
	private static final int BLUR_WIDTH = 10;
	private static final float BLUR_SCALE = 1.0F;
	private static final float BLUR_STRENGTH = 0.0F;


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
		glBackgroundList.open();
		{
			drawBorder();
		}
		glBackgroundList.close();

		glOrganismList.open();
		{
			theEnvironment.glDraw();
		}
		glOrganismList.close();

		/* 
		 * At this point, all drawing is queued up on lists. 
		 * all that's left to render them is a call to glCallList
		 */

		glEffects.bind();
		{
			clearGraphics();
			glOrganismList.call();
		}
		glEffects.unbind();
		{
			clearGraphics();
			glBackgroundList.call();
		}

		// update the display (i.e. swap buffers, etc)
		Display.update();
	}

	private void renderFullScreenQuadTex(){
		
		glColor3f(1,1,1);
		glBegin(GL_QUADS);
		{
			glTexCoord2f(0, 0); glVertex2f(-1.0F, -1.0F);
			glTexCoord2f(1, 0); glVertex2f( 1.0F, -1.0F);
			glTexCoord2f(1, 1); glVertex2f( 1.0F,  1.0F);
			glTexCoord2f(0, 1); glVertex2f(-1.0F,  1.0F);
		}
		glEnd();
	}

	private void clearGraphics(){
		// clear screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		// clear modelview matrix
		glLoadIdentity();
		// move camera (i.e. do glTranslate to move the objects on the screen, as if the camera were moving)
		camera.glSetView();
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
		GLList[] glLists = GLList.createLists(2);
		glOrganismList = glLists[0];
		glBackgroundList = glLists[1];
		// 2d, so save time by not depth-testing
		glDisable(GL_DEPTH_TEST);
		// set up line antialiasing
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_LINE_SMOOTH);
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
				vDefault = Shader.fromSource("../resources/transform.vs", GL_VERTEX_SHADER);
				vScreenTex = Shader.fromSource("../resources/texturepos.vs", GL_VERTEX_SHADER);
				fBlur = Shader.fromSource("../resources/blur.fs", GL_FRAGMENT_SHADER);
				fBlend = Shader.fromSource("../resources/blend.fs", GL_FRAGMENT_SHADER);
				fDefault = Shader.fromSource("../resources/blend.fs", GL_FRAGMENT_SHADER);

				// create programs from the shaders
				pHBlur = Program.createProgram(vDefault, fBlur);
				pVBlur = Program.createProgram(vDefault, fBlur);	
				pBlend = Program.createProgram(vDefault, fBlend);
				pScreenTex = Program.createProgram(vScreenTex, fDefault);
			}
			// create texture and framebuffer
			{
				glEffects = new FrameBuffer(width, height, 2);
			}
		} else{
			System.err.println("FBO not available");
		}
	}

	private void drawBorder() {
		double[] bounds = theEnvironment.getBounds();
		double xmin = bounds[0];
		double ymin = bounds[1];
		double xmax = bounds[2];
		double ymax = bounds[3];
		glColor3f(1f,1f,1f);
		glBegin(GL_LINES);
		glVertex2d(xmin, ymin);
		glVertex2d(xmax, ymin);

		glVertex2d(xmax, ymin);
		glVertex2d(xmax, ymax);

		glVertex2d(xmax, ymax);
		glVertex2d(xmin, ymax);

		glVertex2d(xmin, ymax);
		glVertex2d(xmin, ymin);

		glEnd();
	}

	public void destroy(){
		// destroy programs
		if(pHBlur != null) pHBlur.destroy();
		if(pVBlur != null) pVBlur.destroy();
		if(pBlend != null) pBlend.destroy();
		if(pScreenTex != null) pScreenTex.destroy();
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
