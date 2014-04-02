package graphics.opengl;

import static org.lwjgl.opengl.GL20.*;

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
		
		return new Program(id);
	}
	
	public static Program createProgram(String vSource, String fSource){
		return createProgram(
				Shader.fromSource(vSource, GL_VERTEX_SHADER),
				Shader.fromSource(fSource, GL_FRAGMENT_SHADER));
	}
	
	public void begin(){
		glUseProgram(glId);
	}
	
	public void end(){
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
	
	public int getAttribute(String name){
		return glGetAttribLocation(glId, name);
	}
	
	public int getUniform(String name){
		return glGetUniformLocation(glId, name);
	}
}
