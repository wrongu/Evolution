package graphics.opengl;

import java.awt.Canvas;
import java.nio.FloatBuffer;

import javax.swing.JApplet;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import applet.ControlPanel;
import applet.ControlPanel.ControlListener;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

/* TODO list
 * element arrays
 */

public class EffectsTest extends JApplet implements ControlListener {

	private static final long serialVersionUID = 7064505951633558969L;

	private static final FloatBuffer eye4 = BufferUtils.createFloatBuffer(16);
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

	private FrameBuffer glowMap, glowMap2;
	private Program pDefault, pBlur;
	private VBOProgram vboDefault, vboBlur;
	private FloatBuffer modelViewMatrix, projectionMatrix;

	private int gauss_kernel_size;
	private float[] gaussian;
	private int texGaussId;

	// DEBUGGING
	private ControlPanel ctlPanel;
	boolean updated_flag;

	public void init(){
		setSize(600, 600);
		setVisible(true);
		Canvas canvas = new Canvas();		
		getContentPane().add(canvas);
		try {
			Display.setParent(canvas);
			Display.create();
		}
		catch (LWJGLException e) {
			e.printStackTrace();
		}

//		ctlPanel = new ControlPanel(this);

		System.out.println("OpenGL version is "+glGetString(GL_VERSION));

		initGL();
		initGLAdvanced();

		float[] tex = new float[]{
				1f, 0f, 0f,   0f, 1f, 0f,
				0f, 0f, 1f,   1f, 1f, 1f
		};
		FloatBuffer texBuff = BufferUtils.createFloatBuffer(12);
		texBuff.put(tex);
		texBuff.flip();

		int sillyTex = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, sillyTex);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 2, 2, 0, GL_RGB, GL_FLOAT, texBuff);

		projectionMatrix = BufferUtils.createFloatBuffer(16);
		modelViewMatrix  = BufferUtils.createFloatBuffer(16);

		while(!Display.isCloseRequested()){
			if(updated_flag){
				updated_flag = false;
				createGaussTexture();
			}
			
			// reset graphics
			glBindTexture(GL_TEXTURE_2D, 0); // thanks to FBOExample

			// render thingy (red diamond on top of white square) to glow map
			glowMap.bind();
			{
				clearGraphics();
				drawThingy();
			}
			glowMap.unbind();

			// render texture from frame buffer
			glEnable(GL_TEXTURE_2D);
			clearGraphics();

			glGetFloat(GL_MODELVIEW_MATRIX, modelViewMatrix);
			glGetFloat(GL_MODELVIEW_MATRIX, projectionMatrix);

			vboDefault.setUniformMat("mModelView", modelViewMatrix);
			vboDefault.setUniformMat("mProjection", projectionMatrix);

			glColor3f(1f, 1f, 1f);
			glowMap.bindTex();
			drawTexSquareArray(vboDefault, -0.6f, -0.1f, -0.6f, -0.1f);
			blurGlowMap();
			glActiveTexture(GL_TEXTURE0);
			glowMap.bindTex();
			drawTexSquareArray(vboDefault, -0.6f, -0.1f, 0.1f, 0.6f);

			glBindTexture(GL_TEXTURE_2D, sillyTex);
			drawTexSquareArray(vboDefault, 0.1f, 0.6f, -0.6f, -0.1f);
			drawTexSquareImmediate(0.1f, 0.6f, 0.1f, 0.6f);

			glDisable(GL_TEXTURE_2D);

			// flush (show) graphics, limit fps
			Display.update();
			Display.sync(30);
		}

		// clean up
		glowMap.destroy();
		glowMap2.destroy();
		vboDefault.destroy();
		Display.destroy();
	}

	private void clearGraphics(){
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glLoadIdentity();
	}

	private void initGL(){
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(-1, 1, -1, 1, -1, 1);
		glMatrixMode(GL_MODELVIEW);
		glViewport(0, 0, 600, 600);

		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHTING);
		glDisable(GL_FOG);
		glClearColor(0f, 0f, 0f, 0f);
	}

	private void initGLAdvanced(){
		pDefault = Program.createProgram("../resources/default2D.vs", "../resources/default2D.fs");
		vboDefault = VBOProgram.create(pDefault, "position", false);
		vboDefault.setUniformi("Texture", 0);
		vboDefault.addAttribute("texUV", GL_FLOAT, 2, false, GL_TEXTURE_COORD_ARRAY);
		vboDefault.addAttribute("color", GL_UNSIGNED_BYTE, 4, true, GL_COLOR_ARRAY);
		vboDefault.initComplete();

		// blur setup
		glowMap = new FrameBuffer(600, 600);
		glowMap2 = new FrameBuffer(600, 600);
		pBlur = Program.createProgram("../resources/default2D.vs", "../resources/blur.fs");
		vboBlur = VBOProgram.create(pBlur, "position", false);
		vboBlur.setUniformi("Texture", 0);
		vboBlur.setUniformi("Gaussian", 1);
		vboBlur.addAttribute("texUV", GL_FLOAT, 2, false, GL_TEXTURE_COORD_ARRAY);
		vboBlur.addAttribute("color", GL_UNSIGNED_BYTE, 4, true, GL_COLOR_ARRAY);
		vboBlur.initComplete();

		// precompute gaussian
		gauss_kernel_size = 64;
		texGaussId = glGenTextures();
		createGaussTexture();
	}

	private void createGaussTexture(){
		gaussian = getGaussian(gauss_kernel_size, 0.1f, 0.8f);
		// TODO - does this need to be a power of 2?
		FloatBuffer gfb = BufferUtils.createFloatBuffer(gauss_kernel_size);
		gfb.put(gaussian);
		gfb.flip();
		// create 1D texture for gaussian
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_1D, texGaussId);
		glTexImage1D(GL_TEXTURE_1D, 0, GL_RED, gauss_kernel_size, 0, GL_RED, GL_FLOAT, gfb);
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

	private void blurGlowMap(){
		// make sure guassian 1d texture is bound to sampler 1
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_1D, texGaussId);
		// make sure glowMap (unblurreD) is bound to sampler 0
		glActiveTexture(GL_TEXTURE0);
		glowMap.bindTex();

		// no-transform matrices
		vboBlur.setUniformMat("mModelView", eye4);
		vboBlur.setUniformMat("mProjection", eye4);

		// horizontal blur
		vboBlur.setUniformi("Orientation", 0);
		vboBlur.setUniformi("BlurWidth", 10);
		vboBlur.setUniformi("TextureWidth", 100);

		// draw the glowMap to the framebuffer glowMap2 using the blur shader 
		glowMap2.bind();
		{
			clearGraphics();
			vboBlur.beginDrawing(GL_QUADS);
			{
				vboBlur.setAttributei("color", 0xFFFFFFFF);
				vboBlur.addVertexWithAttribute(-1f, -1f, "texUV", 0f, 0f);
				vboBlur.addVertexWithAttribute( 1f, -1f, "texUV", 1f, 0f);
				vboBlur.addVertexWithAttribute( 1f,  1f, "texUV", 1f, 1f);
				vboBlur.addVertexWithAttribute(-1f,  1f, "texUV", 0f, 1f);
			}
			vboBlur.draw();
		}
		glowMap2.unbind();

		
		// vertical blur
		vboBlur.setUniformi("Orientation", 1);
		// make sure glowMap2 (already horizontal blurred) is bound to sampler 0
		glActiveTexture(GL_TEXTURE0);
		glowMap2.bindTex();
		
		glowMap.bind();
		{
			clearGraphics();
			vboBlur.beginDrawing(GL_QUADS);
			{
				vboBlur.setAttributei("color", 0xFFFFFFFF);
				vboBlur.addVertexWithAttribute(-1f, -1f, "texUV", 0f, 0f);
				vboBlur.addVertexWithAttribute( 1f, -1f, "texUV", 1f, 0f);
				vboBlur.addVertexWithAttribute( 1f,  1f, "texUV", 1f, 1f);
				vboBlur.addVertexWithAttribute(-1f,  1f, "texUV", 0f, 1f);
			}
			vboBlur.draw();
		}
		glowMap.unbind();
	}

	private void drawSquare(float l, float r, float b, float t){
		glBegin(GL_QUADS);
		{
			glVertex2f(l, b);
			glVertex2f(r, b);
			glVertex2f(r, t);
			glVertex2f(l, t);
		}
		glEnd();
	}

	private void drawTexSquareImmediate(float l, float r, float b, float t){
		glBegin(GL_QUADS);
		{
			glTexCoord2f(0f, 0f); glVertex2f(l, b);
			glTexCoord2f(1f, 0f); glVertex2f(r, b);
			glTexCoord2f(1f, 1f); glVertex2f(r, t);
			glTexCoord2f(0f, 1f); glVertex2f(l, t);
		}
		glEnd();
	}

	private static void drawTexSquareArray(VBOProgram vbo, float l, float r, float b, float t){
		vbo.beginDrawing(GL_QUADS);
		{
			vbo.setAttributei("color", (int) 0xFFFFFFFF);
			vbo.addVertexWithAttribute(l, b, "texUV", 0f, 0f);
			vbo.addVertexWithAttribute(r, b, "texUV", 1f, 0f);
			vbo.addVertexWithAttribute(r, t, "texUV", 1f, 1f);
			vbo.addVertexWithAttribute(l, t, "texUV", 0f, 1f);
		}
		vbo.draw();
	}

	private void drawThingy(){
		glColor3f(1f, 1f, 1f);
		drawSquare(-0.5f, 0.5f, -0.5f, 0.5f);
		glRotatef(45f, 0f, 0f, 1f);
		glColor3f(1f, 0f, 0f);
		drawSquare(-0.5f, 0.5f, -0.5f, 0.5f);
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

	public void valuesUpdated() {
		gauss_kernel_size = ctlPanel.getKernel();
		updated_flag = true;
	}
}
