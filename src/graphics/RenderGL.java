package graphics;

import java.awt.Canvas;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JPanel;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;

import environment.Environment;

public class RenderGL {

	private static final long serialVersionUID = -5610531621479479038L;

	private Environment theEnvironment;
	private Camera camera;
	private int width, height;
	private boolean[] keyboard;
	private int[] mouse;
	// opengl is a state machine that gives us references to its objects as ints.
	private int glDisplayList;
	// the following opengl references are all for the glow effect. Requires FBO to be enabled.
	// TODO - implement checks for FBOEnabled
	private static boolean FBOEnabled; 
	private int glVertexShader, glFragmentShader;
	private int glShaderProgram;
	private int glFrameBuffer;
	private int glTexRenderedLines;

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
		// start list compilation and write all draw() operations to that list
		glNewList(glDisplayList, GL_COMPILE);
		{
			// move camera (i.e. do glTranslate to move the objects on the screen, as if the camera were moving)
			camera.glSetView();
			glColor3f(1f,1f,1f);
			glBegin(GL_LINES);
			glVertex2f(0,0);
			glVertex2f(width,0);
			

			glVertex2f(width,0);
			glVertex2f(width,height);
			
			glVertex2f(width,height);
			glVertex2f(0,height);

			glVertex2f(0,height);
			glVertex2f(0,0);
			
			glEnd();
			theEnvironment.glDraw();
		}
		glEndList();
		// At this point, all drawing is queued up in the list glDisplayList. to actually render it is just
		//	a call to glCallList
		//
		// switch to FBO
		glBindTexture(GL_TEXTURE_2D, 0);		// clear texture to avoid "problems". IDK why - the demo does it.
		bindFBO(glFrameBuffer);
		{
			clearGraphics();
			glCallList(glDisplayList);
		}
		// switch back to main screen
		unbindFBO();
		// clear the main screen
		clearGraphics();
		glCallList(glDisplayList);
		Display.update();
	}

	@Deprecated
	/**
	 * This function renders a quad that fills the screen with the current texture.
	 * Originally it was planned for use with render-to-texture (on the FBO), but using lists we can just call the list multiple times instead.
	 */
	private void renderFullScreenQuadTex(){
		glColor3f(1,1,1);
		glBegin(GL_QUADS);
		{
			glTexCoord2f(0, 0); glVertex2f(0, 0);
			glTexCoord2f(1, 0); glVertex2f(width, 0);
			glTexCoord2f(1, 1); glVertex2f(width, height);
			glTexCoord2f(0, 1); glVertex2f(0, height);
		}
		glEnd();
	}
	
	private void clearGraphics(){
		// clear screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
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
		// load and compile shaders
		initGLShaders();
	}

	private void initGLShaders(){
		FBOEnabled = GLContext.getCapabilities().GL_EXT_framebuffer_object;
		if(FBOEnabled){
			// create shaders and their associated program
			{
				glShaderProgram = glCreateProgram();
				glVertexShader = loadShader("../resources/shader.vert", GL_VERTEX_SHADER);
				glFragmentShader = loadShader("../resources/shader.frag", GL_FRAGMENT_SHADER);
				glAttachShader(glShaderProgram, glVertexShader);
				glAttachShader(glShaderProgram, glFragmentShader);
				glLinkProgram(glShaderProgram);
				glValidateProgram(glShaderProgram);
			}
			// create texture and framebuffer
			{
				glTexRenderedLines = glGenTextures();
				glFrameBuffer = glGenFramebuffersEXT();
				glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, glFrameBuffer);
				{
					glBindTexture(GL_TEXTURE_2D, glTexRenderedLines);
					//					glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
					//					glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0,GL_RGBA, GL_INT, (java.nio.ByteBuffer) null);
					glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, glTexRenderedLines, 0);
					glMatrixMode(GL_PROJECTION);
					glLoadIdentity();
					glOrtho(0, width, 0, height, -1, 1);
					glMatrixMode(GL_MODELVIEW);
				}
			}
			glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
		} else{
			System.err.println("FBO not available");
		}
	}

	private void bindFBO(int id){
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, glFrameBuffer);
		glPushAttrib(GL_VIEWPORT_BIT);
		glViewport(0, 0, width, height);
		glClear(GL_COLOR_BUFFER_BIT);
		// TODO - when we run the pedantic check, there is an error. If we skip the check, it works fine... hm.
		//		checkFBO();
	}

	private void unbindFBO(){
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
		glPopAttrib();
	}

	private void checkFBO(){
		int framebuffer = glCheckFramebufferStatusEXT( GL_FRAMEBUFFER_EXT ); 
		switch ( framebuffer ) {
		case GL_FRAMEBUFFER_COMPLETE_EXT:
			break;
		case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
			throw new RuntimeException( "FrameBuffer: " + glFrameBuffer
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
			throw new RuntimeException( "FrameBuffer: " + glFrameBuffer
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
			throw new RuntimeException( "FrameBuffer: " + glFrameBuffer
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
			throw new RuntimeException( "FrameBuffer: " + glFrameBuffer
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
			throw new RuntimeException( "FrameBuffer: " + glFrameBuffer
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
			throw new RuntimeException( "FrameBuffer: " + glFrameBuffer
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT exception" );
		default:
			throw new RuntimeException( "Unexpected reply from glCheckFramebufferStatusEXT: " + framebuffer );
		}
	}

	public void destroy(){
		if(glShaderProgram != 0) 	glDeleteProgram(glShaderProgram);
		if(glVertexShader != 0) 	glDeleteShader(glVertexShader);
		if(glFragmentShader != 0) glDeleteShader(glFragmentShader);
		Display.destroy();
	}

	public void bindInputs(boolean[] direction_keys, int[] mouse_move) {
		keyboard = direction_keys;
		mouse = mouse_move;
	}

	public int loadShader(String source, int shaderType){
		try{
			// opengl has its own shader language. So, in another file (sepecified by 'source'),
			//	there is a program written in that language. We read it into a stringbuilder
			//	then build the shader and compile it from that stringbuilder.
			BufferedReader buff = new BufferedReader(new FileReader(source));
			StringBuilder builder = new StringBuilder();
			String line;
			while((line = buff.readLine()) != null)
				builder.append(line).append('\n');
			buff.close();
			// create the shader
			int shader = glCreateShader(shaderType);
			// bind the source to the shader and compile it
			glShaderSource(shader, builder);
			glCompileShader(shader);
			// check compilation
			if(glGetShader(shader, GL_COMPILE_STATUS) == GL_FALSE){
				System.err.println("error compiling shader from " + source);
				glDeleteShader(shader);
				return 0;
			}
			System.out.println("loaded shader " + source);
			return shader;
		} catch(IOException e){
			System.err.println("could not open resource " + source);
			return 0;
		}
	}
}
