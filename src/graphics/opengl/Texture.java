package graphics.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;

import java.nio.ByteBuffer;

public class Texture {
	
	private int glId;
	private int active_layer;
	
	private Texture(int id){
		glId = id;
		active_layer = 0;
	}
	
	/**
	 * create a texture with linear interpolation and the given width/height
	 * @param w width
	 * @param h height
	 * @return a texture object that provides abstracted access to openGL texture functions
	 */
	public static Texture create(int w, int h){
		int id = glGenTextures();
		Texture tex = new Texture(id);
		glBindTexture(GL_TEXTURE_2D, id);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w, h, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
		tex.setFilter(GL_LINEAR);
		tex.setWrap(GL_CLAMP_TO_EDGE);
		return tex;
	}
	
	public void setFilter(int type){
		glBindTexture(GL_TEXTURE_2D, glId);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, type);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, type);
	}
	
	public void setWrap(int type){
		int prev_tex = glGetInteger(GL_TEXTURE_BINDING_2D);
		glBindTexture(GL_TEXTURE_2D, glId);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, type);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, type);
		glBindTexture(GL_TEXTURE_2D, prev_tex);
	}
	
	public void activate(){
		glActiveTexture(GL_TEXTURE0 + active_layer);
	}
	
	public int getActiveLayer(){
		return active_layer;
	}
	
	public int getId(){
		return glId;
	}

	public void destroy() {
		glDeleteTextures(glId);
	}
	
}
