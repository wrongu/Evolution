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
		if(num_textures > 16){
			num_textures = 16;
			System.err.println("Cannot have more than 16 textures per frame buffer");
		}
		glId = glGenFramebuffersEXT();
		for(int i = 0; i < num_textures; i++){
			Texture tex = Texture.create(w, h);
			textures[i] = tex;
		}
		
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, glId);
		{
			// TODO - not sure how to do multiple textures here
			glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, textures[0].getId(), 0);
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

	public void bind(int tex){
		glPushMatrix();
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, glId);
		glPushAttrib(GL_VIEWPORT_BIT);
		glViewport(0, 0, width, height);
		bindTex(tex);
	}

	public void bindTex(int tex){
		glBindTexture(GL_TEXTURE_2D, textures[tex].getId());
	}

	public void unbind(){
		glPopAttrib();
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
		glPopMatrix();
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
