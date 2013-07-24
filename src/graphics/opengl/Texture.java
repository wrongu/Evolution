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
		Texture tex = (new Texture(id)).setFilter(GL_LINEAR).setWrap(GL_CLAMP_TO_EDGE);;
		glBindTexture(GL_TEXTURE_2D, id);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w, h, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
		return tex;
	}
	
	public Texture setActiveLayer(int layer){
		active_layer = layer;
		return this;
	}
	
	public void bind(){
		activate();
		glBindTexture(GL_TEXTURE_2D, glId);
	}
	
	public Texture setFilter(int type){
		bind();
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, type);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, type);
		return this;
	}
	
	public Texture setWrap(int type){
		bind();
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, type);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, type);
		return this;
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
