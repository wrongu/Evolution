package graphics.opengl;

import java.awt.Canvas;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.swing.JApplet;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL40.*;

/* TODO list
 * element arrays
 * multiple textures per FBO
 */

public class EffectsTest extends JApplet {

	private static final long serialVersionUID = 7064505951633558969L;

	private PrimitiveArray gl_array;

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

		initGL();

		float[] tex = new float[]{
				1f, 0f, 0f,   0f, 1f, 0f,
				0f, 0f, 1f,   1f, 1f, 1f
		};
		FloatBuffer texBuff = BufferUtils.createFloatBuffer(12);
		texBuff.put(tex);
		texBuff.flip();

		int sillyTex = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, sillyTex);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL_RGBA8, 2, 2, 0, GL_RGB, GL_FLOAT, texBuff);

		FrameBuffer fb = new FrameBuffer(600, 600);

		while(!Display.isCloseRequested()){
			// reset graphics
			glBindTexture(GL_TEXTURE_2D, 0); // thanks to FBOExample

			// render graphics to frame buffer
			fb.bind();
			{
				clearGraphics();
				drawThingy();
			}
			fb.unbind();

			// render texture from frame buffer
			glEnable(GL_TEXTURE_2D);
			clearGraphics();

			GL11.glColor3f(1f, 1f, 1f);
			fb.bindTex();
			drawTexSquareArray(-0.6f, -0.1f, -0.6f, -0.1f);
			drawTexSquare(-0.6f, -0.1f, 0.1f, 0.6f);
			
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, sillyTex);
			drawTexSquareArray(0.1f, 0.6f, -0.6f, -0.1f);
			drawTexSquare(0.1f, 0.6f, 0.1f, 0.6f);

			glDisable(GL_TEXTURE_2D);

			// flush (show) graphics, limit fps
			Display.update();
			Display.sync(60);
		}

		// clean up
		fb.destroy();
		gl_array.destroy();
		Display.destroy();
	}

	private void clearGraphics(){
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glLoadIdentity();
	}

	private void initGL(){
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(-1, 1, -1, 1, -1, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glViewport(0, 0, 600, 600);

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glClearColor(0f, 0.8f, 0.8f, 1f);

		gl_array = PrimitiveArray.create(1);
	}

	private void drawSquare(float l, float r, float b, float t){
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glVertex2f(l, b);
			GL11.glVertex2f(r, b);
			GL11.glVertex2f(r, t);
			GL11.glVertex2f(l, t);
		}
		GL11.glEnd();
	}

	private void drawTexSquare(float l, float r, float b, float t){
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glTexCoord2f(0f, 0f); GL11.glVertex2f(l, b);
			GL11.glTexCoord2f(1f, 0f); GL11.glVertex2f(r, b);
			GL11.glTexCoord2f(1f, 1f); GL11.glVertex2f(r, t);
			GL11.glTexCoord2f(0f, 1f); GL11.glVertex2f(l, t);
		}
		GL11.glEnd();
	}

	private void drawTexSquareArray(float l, float r, float b, float t){
		gl_array.beginDrawing(GL11.GL_QUADS);
		{
			gl_array.addTexVertex(l, b, 0, 0);
			gl_array.addTexVertex(r, b, 1, 0);
			gl_array.addTexVertex(r, t, 1, 1);
			gl_array.addTexVertex(l, t, 0, 1);
		}
		gl_array.draw();
	}

	private void drawThingy(){
		GL11.glColor3f(1f, 1f, 1f);
		drawSquare(-0.5f, 0.5f, -0.5f, 0.5f);
		GL11.glRotatef(45f, 0f, 0f, 1f);
		GL11.glColor3f(1f, 0f, 0f);
		drawSquare(-0.5f, 0.5f, -0.5f, 0.5f);
	}
}
