package graphics.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;

public class FrameBuffer {

	private int glId;
	private Texture[] textures;
	private int width, height;
	
	public FrameBuffer(int w, int h, int num_textures){
		width = w;
		height = h;
		textures = new Texture[num_textures];
		glId = glGenFramebuffersEXT();
		
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, glId);
		{
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			glOrtho(0, w, 0, h, -1, 1);
			glMatrixMode(GL_MODELVIEW);
			
			for(int i = 0; i < num_textures; i++){
				Texture tex = Texture.create(w, h);
				glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, tex.getId(), 0);
				textures[i] = tex;
			}
		}
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
		checkFBO();
	}

	public Texture getTexture(int index){
		if(index >= 0 && index < textures.length)
			return textures[index];
		else
			return null;
	}

	public void bind(){
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, glId);
		glPushAttrib(GL_VIEWPORT_BIT);
		glViewport(0, 0, width, height);
	}

	public void bind(int tex){
		bind();
		textures[tex].activate();
	}

	public void unbind(){
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
		glPopAttrib();
	}
	
	public void destroy(){
		for(Texture t : textures)
			if(t != null) t.destroy();
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

}
