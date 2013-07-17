package graphics.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import java.nio.ByteBuffer;

public class Texture {
	
	private int glId;
	private int active_layer;
	
	private Texture(int id){
		glId = id;
		active_layer = 0;
	}
	
	public static Texture create(int w, int h){
		int id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, w, h, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
		return new Texture(id);
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
