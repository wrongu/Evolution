package graphics.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.EXTFramebufferObject.*;

public class FrameBuffer {

	private int glId;
	private Texture texture;
	private int width, height;
	
	public FrameBuffer(int w, int h){
		width = w;
		height = h;
		glId = glGenFramebuffersEXT();
		texture = Texture.create(w, h);
		
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, glId);
		{
			glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, getTexture().getId(), 0);
		}
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
		checkFBO();
	}

	public void bind(){
		glPushMatrix();
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, glId);
		glPushAttrib(GL_VIEWPORT_BIT);
		glViewport(0, 0, width, height);
	}

	public void bindTex(){
		glBindTexture(GL_TEXTURE_2D, getTexture().getId());
	}
	
	public void bindTex(int active_layer){
		glActiveTexture(GL_TEXTURE0 + active_layer);
		glBindTexture(GL_TEXTURE_2D, getTexture().getId());
	}

	public void unbind(){
		glPopAttrib();
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
		glPopMatrix();
	}
	
	public void destroy(){
		getTexture().destroy();
		glDeleteFramebuffersEXT(glId);
	}

	private void checkFBO(){
		int framebuffer = glCheckFramebufferStatusEXT( GL_FRAMEBUFFER_EXT ); 
		switch ( framebuffer ) {
		case GL_FRAMEBUFFER_COMPLETE_EXT:
			break;
		case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
			throw new RuntimeException( "FrameBuffer: " + glId
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
			throw new RuntimeException( "FrameBuffer: " + glId
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
			throw new RuntimeException( "FrameBuffer: " + glId
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
			throw new RuntimeException( "FrameBuffer: " + glId
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
			throw new RuntimeException( "FrameBuffer: " + glId
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
			throw new RuntimeException( "FrameBuffer: " + glId
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT exception" );
		default:
			throw new RuntimeException( "Unexpected reply from glCheckFramebufferStatusEXT: " + framebuffer );
		}
	}

	public Texture getTexture() {
		return texture;
	}

}
