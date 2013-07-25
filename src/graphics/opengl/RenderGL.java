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
import static org.lwjgl.opengl.GL20.*;


import environment.Environment;
import graphics.Camera;

public class RenderGL {

	//	private static final long serialVersionUID = -5610531621479479038L;

	private Environment theEnvironment;
	private Camera camera;
	private int width, height;
	private double initHeight;
	private boolean[] keyboard;
	private int[] mouse_buttons;

	private FloatBuffer modelViewMatrix, projectionMatrix;
	// identity matrix for those no-transform renderings
	private static final FloatBuffer eye4 = BufferUtils.createFloatBuffer(16);

	// the following opengl references are all for the glow effect. Requires FBO to be enabled.
	private static boolean FBOEnabled;
	private VBOProgram pDefault, pBlur, pBlend, pNoTex;
	private FrameBuffer preRenderScene, glowMap, glowMap2;
	private int glGaussTex;

	private static final int GLOW_MODE = 0; // additive - high overexposure effect
	//	private static final int GLOW_MODE = 1; // screen blending - medium effect
	//	private static final int GLOW_MODE = 2; // soft lighting - no overexposure
	//		private static final int GLOW_MODE = 3; // show raw glow texture
	private static final int BLUR_WIDTH = 5;
	private static final int BLUR_KERNEL = 64;
	private static final int BLUR_RESOLUTION = 600;
	private static final float GAUSS_PEAK = 1.5f;
	private static final float GAUSS_WIDTH = 0.3f;

	public RenderGL(Canvas canvas, Environment env, int w, int h){
		// set up panel with respect to the evolution app
		theEnvironment = env;
		width = w;
		height = h;
		// keeping track of initial height so that aspect ratio can be preserved when window moves
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

		modelViewMatrix = BufferUtils.createFloatBuffer(16);
		projectionMatrix = BufferUtils.createFloatBuffer(16);
	}

	public void redraw(){
		// in case screen size changed
		width = Display.getWidth();
		height = Display.getHeight();
		glWindowSize();

		glBindTexture(GL_TEXTURE_2D, 0);

		// pre-render: everything to the pre-render frame buffer
		preRenderScene.bind();
		{
			// reset this framebuffer and its transformation matrices
			clearGraphics();
			// move camera (i.e. do glTranslate to move the objects on the screen, as if the camera were moving)
			camera.glSetView();

			// load current view matrices into FloatBuffers and pass them to the program/shader
			glGetFloat(GL_MODELVIEW_MATRIX, modelViewMatrix);
			glGetFloat(GL_PROJECTION_MATRIX, projectionMatrix);
			// set matrices for pDefault (the no-transform program)
			pNoTex.setUniformMat("mModelView", modelViewMatrix);
			pNoTex.setUniformMat("mProjection", projectionMatrix);

			// begin line drawings
			// TODO - what if we're drawing something other than lines? Maybe we need to split glDraw into glDrawLines, glDrawQuads,
			//	etc.. they will be empty for entities that don't draw that type
			pNoTex.beginDrawing(GL_LINES);
			{
				theEnvironment.glDraw(pNoTex);
				drawBorder(pNoTex);
			}
			pNoTex.draw();
		}
		preRenderScene.unbind();

		// TODO - instead of 2 passes, use multiple render targets?
		// second pass: glowmap only
		glowMap.bind();
		{
			// reset this framebuffer and its transformation matrices.
			// note that there is no camera involved here since it was already moved and the uniform matrices update last pass.
			clearGraphics();

			// begin line drawings
			pNoTex.beginDrawing(GL_LINES);
			{
				theEnvironment.glDraw(pNoTex);
			}
			pNoTex.draw();
		}
		glowMap.unbind();

		glEnable(GL_TEXTURE_2D);

		// blur
		blurGlowMap();

		// blend and render to the screen
		glActiveTexture(GL_TEXTURE1);
		glowMap.bindTex();
		glActiveTexture(GL_TEXTURE0);
		preRenderScene.bindTex();

		clearGraphics();

		pBlend.beginDrawing(GL_QUADS);
		{
			pBlend.setColor(1f, 1f, 1f, 1f);
			pBlend.addVertexWithTex(-1f, -1f, 0f, 0f);
			pBlend.addVertexWithTex( 1f, -1f, 1f, 0f);
			pBlend.addVertexWithTex( 1f,  1f, 1f, 1f);
			pBlend.addVertexWithTex(-1f,  1f, 0f, 1f);
		}
		pBlend.draw();

		glDisable(GL_TEXTURE_2D);

		// update the display (i.e. swap buffers, etc)
		Display.update();
	}

	private void blurGlowMap(){
		// make sure guassian 1d texture is bound to sampler 1
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_1D, glGaussTex);
		// make sure glowMap (unblurreD) is bound to sampler 0
		glActiveTexture(GL_TEXTURE0);
		glowMap.bindTex();

		// horizontal blur
		pBlur.setUniformi("Orientation", 0);

		// draw the glowMap to the framebuffer glowMap2 using the blur shader 
		glowMap2.bind();
		{
			clearGraphics();
			pBlur.beginDrawing(GL_QUADS);
			{
				pBlur.setColor(1f, 1f, 1f, 1f);
				pBlur.addVertexWithTex(-1f, -1f, 0f, 0f);
				pBlur.addVertexWithTex( 1f, -1f, 1f, 0f);
				pBlur.addVertexWithTex( 1f,  1f, 1f, 1f);
				pBlur.addVertexWithTex(-1f,  1f, 0f, 1f);
			}
			pBlur.draw();
		}
		glowMap2.unbind();


		// vertical blur
		pBlur.setUniformi("Orientation", 1);
		// make sure glowMap2 (already horizontal blurred) is bound to sampler 0
		glActiveTexture(GL_TEXTURE0);
		glowMap2.bindTex();
		// render the result back to the original glowmap
		glowMap.bind();
		{
			clearGraphics();
			pBlur.beginDrawing(GL_QUADS);
			{
				pBlur.setColor(1f, 1f, 1f, 1f);
				pBlur.addVertexWithTex(-1f, -1f, 0f, 0f);
				pBlur.addVertexWithTex( 1f, -1f, 1f, 0f);
				pBlur.addVertexWithTex( 1f,  1f, 1f, 1f);
				pBlur.addVertexWithTex(-1f,  1f, 0f, 1f);
			}
			pBlur.draw();
		}
		glowMap.unbind();
	}

	private void clearGraphics(){
		// clear screen
		glClear(GL_COLOR_BUFFER_BIT);
		// clear modelview matrix
		glLoadIdentity();
	}

	public void moveCamera(double dt) {
		double dx = 0.0, dy = 0.0;
		if(keyboard[0]) dy -= .001*dt;
		if(keyboard[1]) dy += .001*dt;
		if(keyboard[2]) dx += .001*dt;
		if(keyboard[3]) dx -= .001*dt;
		camera.shift(dx, dy);
		camera.zoom((double) mouse_buttons[1] * 0.00005);
	}

	private void initGL(){
		glWindowSize();
		// 2d, so save time by not depth-testing
		glDisable(GL_DEPTH_TEST);
		// set up line antialiasing
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_LINE_SMOOTH);
		// background clear color is black
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		// load and compile shaders
		initGLShaders();
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
			// create framebuffers with initial width and height (should preserve aspect ratio, I hope!)
			preRenderScene = new FrameBuffer(width, height);
			glowMap = new FrameBuffer(width, height);
			glowMap2 = new FrameBuffer(width, height);

			Shader vDefault = Shader.fromSource("../resources/default2D.vs", GL_VERTEX_SHADER);
			Shader fDefault = Shader.fromSource("../resources/default2D.fs", GL_FRAGMENT_SHADER);
			Shader fBlur    = Shader.fromSource("../resources/blur.fs", GL_FRAGMENT_SHADER);
			Shader fBlend   = Shader.fromSource("../resources/blend.fs", GL_FRAGMENT_SHADER);

			// create programs
			Program def = Program.createProgram(vDefault, fDefault);
			Program blur = Program.createProgram(vDefault, fBlur);
			Program blend = Program.createProgram(vDefault, fBlend);
			Program notex = Program.createProgram("../resources/notex2D.vs", "../resources/notex2D.fs");

			// create vbo programs
			pDefault = VBOProgram.create(def, "position", false);
			{
				pDefault.addAttribute("texUV", GL_FLOAT, 2, false, GL_TEXTURE_COORD_ARRAY);
				pDefault.addAttribute("color", GL_UNSIGNED_BYTE, 4, true, GL_COLOR_ARRAY);
				pDefault.initComplete();
				pDefault.setUniformi("Texture", 0);
				System.out.println("created default vbo program");
			}
			pBlur = VBOProgram.create(blur, "position", false);
			{
				pBlur.addAttribute("texUV", GL_FLOAT, 2, false, GL_TEXTURE_COORD_ARRAY);
				pBlur.addAttribute("color", GL_UNSIGNED_BYTE, 4, true, GL_COLOR_ARRAY);
				pBlur.initComplete();
				// set uniforms that will never change here
				pBlur.setUniformMat("mModelView", eye4);
				pBlur.setUniformMat("mProjection", eye4);
				pBlur.setUniformi("BlurWidth", BLUR_WIDTH);
				pBlur.setUniformi("TextureWidth", BLUR_RESOLUTION);
				// set textures
				pBlur.setUniformi("Texture", 0);
				pBlur.setUniformi("Gaussian", 1);
				System.out.println("created blur vbo program");
			}
			pBlend = VBOProgram.create(blend, "position", false);
			{
				pBlend.addAttribute("texUV", GL_FLOAT, 2, false, GL_TEXTURE_COORD_ARRAY);
				pBlend.addAttribute("color", GL_UNSIGNED_BYTE, 4, true, GL_COLOR_ARRAY);
				pBlend.initComplete();
				// set uniforms that will never change here
				pBlend.setUniformMat("mModelView", eye4);
				pBlend.setUniformMat("mProjection", eye4);
				pBlend.setUniformi("BlendMode", GLOW_MODE);
				// set textures
				pBlend.setUniformi("Underlay", 0);
				pBlend.setUniformi("Overlay", 1);
				System.out.println("created blend vbo program");
			} 
			pNoTex = VBOProgram.create(notex, "position", false);
			{
				pNoTex.addAttribute("color", GL_FLOAT, 4, false, GL_COLOR_ARRAY);
				pNoTex.initComplete();
			}

			createGaussTexture();
		} else{
			System.err.println("FBO not available");
		}
	}

	private void drawBorder(VBOProgram vbo) {
		double[] bounds = theEnvironment.getBounds();
		float xmin = (float) bounds[0];
		float ymin = (float) bounds[1];
		float xmax = (float) bounds[2];
		float ymax = (float) bounds[3];
		vbo.setColor(1f, 1f, 1f, 1f);

		vbo.addVertex(xmin, ymin);
		vbo.addVertex(xmax, ymin);

		vbo.addVertex(xmax, ymin);
		vbo.addVertex(xmax, ymax);

		vbo.addVertex(xmax, ymax);
		vbo.addVertex(xmin, ymax);

		vbo.addVertex(xmin, ymax);
		vbo.addVertex(xmin, ymin);
	}

	public void destroy(){
		// destroy programs
		if(pBlur != null) pBlur.destroy();
		if(pBlend != null) pBlend.destroy();
		if(pDefault != null) pDefault.destroy();
		// remove that one 1D texture we have
		glDeleteTextures(glGaussTex);
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

	private void createGaussTexture(){
		float[] gaussian = getGaussian(BLUR_KERNEL, GAUSS_WIDTH, GAUSS_PEAK);
		FloatBuffer gfb = BufferUtils.createFloatBuffer(BLUR_KERNEL);
		gfb.put(gaussian);
		gfb.flip();
		// create 1D texture for gaussian
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_1D, glGaussTex);
		glTexImage1D(GL_TEXTURE_1D, 0, GL_RED, BLUR_KERNEL, 0, GL_RED, GL_FLOAT, gfb);
		glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
		glTexParameterf(GL_TEXTURE_1D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
		FloatBuffer borderColor = BufferUtils.createFloatBuffer(4);
		borderColor.put(new float[]{0f, 0f, 0f, 1f});
		borderColor.flip();
		glTexParameter(GL_TEXTURE_1D, GL_TEXTURE_BORDER_COLOR, borderColor);
		glActiveTexture(GL_TEXTURE0);
	}

	private static float[] getGaussian(int kernel, float sigma2, float peak){
		float max = 0f;
		float[] ret = new float[kernel];
		for(int i = 0; i < kernel; i++){
			// x ranges from -0.5 to 0.5, so that kernel size controls only the resolution
			float x = (float) i / (float) kernel - 0.5f;
			ret[i] = gauss(x, sigma2);
			max = max < ret[i] ? ret[i] : max;
		}
		// peak is enforced by scaling everything so that the max value becomes the desired peak value
		for(int i = 0; i < kernel; i++) ret[i] *= peak / max;

		return ret;
	}

	private static float gauss(float x, float sigma2){
		return (float) ((1.0 / Math.sqrt(2.0 * 3.141592 * sigma2)) * Math.exp(-((x * x) / (2.0 * sigma2))));
	}

	static{
		eye4.put(0, 1);
		eye4.put(1, 0);
		eye4.put(2, 0);
		eye4.put(3, 0);

		eye4.put(4, 0);
		eye4.put(5, 1);
		eye4.put(6, 0);
		eye4.put(7, 0);

		eye4.put(8, 0);
		eye4.put(9, 0);
		eye4.put(10, 1);
		eye4.put(11, 0);

		eye4.put(12, 0);
		eye4.put(13, 0);
		eye4.put(14, 0);
		eye4.put(15, 1);
	}
}
