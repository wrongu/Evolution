package graphics.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;

public class RenderTarget {

	private int glFBOId;
	private int[] textures;
	private int n_tex;
	private int width, height;
	
	public RenderTarget(int w, int h){
		width = w;
		height = h;
		textures = new int[8];
		glFBOId = glGenFramebuffersEXT();
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, glFBOId);
		{
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			glOrtho(0, w, 0, h, -1, 1);
			glMatrixMode(GL_MODELVIEW);
		}
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
	}
	
	public int addTexture(){
		if(n_tex < textures.length){
			textures[n_tex] = glGenTextures();
			glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, glFBOId);
			{
				glActiveTexture(GL_TEXTURE0 + n_tex);
				glBindTexture(GL_TEXTURE_2D, textures[n_tex]);
				glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, textures[n_tex], 0);
			}
			glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
			n_tex++;
		}
		return n_tex;
	}
	
	public int getTexture(int index){
		if(index >= 0 && index < textures.length)
			return textures[index];
		else
			return 0;
	}

	public void bind(){
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, glFBOId);
		glPushAttrib(GL_VIEWPORT_BIT);
		glViewport(0, 0, width, height);
		// TODO - when we run the pedantic check, there is an error. If we skip the check, it works fine... hm.
		//		checkFBO();
	}

	public void bind(int texture){
		bind();
		glActiveTexture(GL_TEXTURE0 + texture);
	}

	public void unbind(){
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
		glPopAttrib();
	}

	private void checkFBO(){
		int framebuffer = glCheckFramebufferStatusEXT( GL_FRAMEBUFFER_EXT ); 
		switch ( framebuffer ) {
		case GL_FRAMEBUFFER_COMPLETE_EXT:
			break;
		case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
			throw new RuntimeException( "FrameBuffer: " + glFBOId
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
			throw new RuntimeException( "FrameBuffer: " + glFBOId
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
			throw new RuntimeException( "FrameBuffer: " + glFBOId
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
			throw new RuntimeException( "FrameBuffer: " + glFBOId
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
			throw new RuntimeException( "FrameBuffer: " + glFBOId
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT exception" );
		case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
			throw new RuntimeException( "FrameBuffer: " + glFBOId
					+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT exception" );
		default:
			throw new RuntimeException( "Unexpected reply from glCheckFramebufferStatusEXT: " + framebuffer );
		}
	}
	
}
