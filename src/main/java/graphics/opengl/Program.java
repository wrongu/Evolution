package graphics.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;

import java.nio.FloatBuffer;

public class Program {
	
	private int glId;
	
	private Program(int id){
		glId = id;
	}
	
	public static Program createProgram(Shader vShader, Shader fShader){
		if(vShader == null || fShader == null) return null;
		
		int id = glCreateProgram();
		
		if(id == 0) return null;
		
		glAttachShader(id, vShader.getId());
		glAttachShader(id, fShader.getId());
		glLinkProgram(id);
		glValidateProgram(id);
		
		System.out.println("Shader linking output:");
		System.out.println(glGetProgramInfoLog(id, 1024));
		
		int status = glGetProgram(id, GL_LINK_STATUS);
		if(status == GL_FALSE){
			System.err.println("Linking error");
			glDeleteProgram(id);
			return null;
		}
		
		return new Program(id);
	}
	
	public static Program createProgram(String vSource, String fSource){
		return createProgram(
				Shader.fromSource(vSource, GL_VERTEX_SHADER),
				Shader.fromSource(fSource, GL_FRAGMENT_SHADER));
	}
	
	public void use(){
		glUseProgram(glId);
	}
	
	public void unuse(){
		glUseProgram(0);
	}
	
	public void destroy(){
		glDeleteProgram(glId);
	}
	
	public void setUniformi(String name, int ... values){
		if(values != null){
			int param = getUniform(name);
			switch(values.length){
			case 1:
				glUniform1i(param, values[0]);
				break;
			case 2:
				glUniform2i(param, values[0], values[1]);
				break;
			case 3:
				glUniform3i(param, values[0], values[1], values[2]);
				break;
			case 4:
				glUniform4i(param, values[0], values[1], values[2], values[3]);
				break;
			default:
				break;
			}
		}
	}
	
	public void setUniformf(String name, float ... values){
		if(values != null){
			int param = getUniform(name);
			switch(values.length){
			case 1:
				glUniform1f(param, values[0]);
				break;
			case 2:
				glUniform2f(param, values[0], values[1]);
				break;
			case 3:
				glUniform3f(param, values[0], values[1], values[2]);
				break;
			case 4:
				glUniform4f(param, values[0], values[1], values[2], values[3]);
				break;
			default:
				break;
			}
		}
	}
	
	public void bindUniformBlock(String blockName, int buffer, int bindTo){
		// get the block id
		int block_id = glGetUniformBlockIndex(glId, blockName);
		if(block_id == GL_INVALID_INDEX){
			System.err.println("No Uniform Block '"+blockName+"'");
			return;
		}
		// bind uniform block to the 'bindTo' port
		glUniformBlockBinding(glId, block_id, bindTo);
		// bind the buffer to the same port
		glBindBufferBase(GL_UNIFORM_BUFFER, bindTo, buffer);
		// congratulations. your buffer and uniform block are now connected
	}
	
	public int getUniformBlockStride(String blockName){
		int block_id = glGetUniformBlockIndex(glId, blockName);
		return glGetActiveUniforms(glId, block_id, GL_UNIFORM_ARRAY_STRIDE);
	}
	
	public void setUniformMat4(String name, FloatBuffer values){
		if(values != null){
			int param = getUniform(name);
			glUniformMatrix4(param, false, values);
		}
	}
	
	public int getAttribute(String name){
		return glGetAttribLocation(glId, name);
	}
	
	public int getUniform(String name){
		return glGetUniformLocation(glId, name);
	}
}
