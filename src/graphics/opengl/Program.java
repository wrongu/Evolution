package graphics.opengl;

import static org.lwjgl.opengl.GL20.*;

public class Program {

	private int glId;
	private Shader vShader, fShader;
	private boolean is_linked;

	private Program(int id, Shader vs, Shader fs){
		glId = id;
		vShader = vs;
		fShader = fs;
		is_linked = false;
	}

	public static Program createProgram(Shader vShader, Shader fShader){
		if(vShader == null || fShader == null) return null;

		int id = glCreateProgram();

		if(id == 0) return null;

		vShader.attach(id);
		fShader.attach(id);

		return new Program(id, vShader, fShader);
	}

	public static Program createProgram(String vSource, String fSource){
		return createProgram(
				Shader.fromSource(vSource, GL_VERTEX_SHADER),
				Shader.fromSource(fSource, GL_FRAGMENT_SHADER));
	}

	public void begin(){
		if(is_linked)
			glUseProgram(glId);
		else
			System.err.println("OpenGL Program: must call link() before using it with begin()");
	}

	public void link(){
		// TODO - must bind shader inputs and outputs before linking!
		glLinkProgram(glId);
		is_linked = true;
	}
	
	public void end(){
		glUseProgram(0);
	}

	/**
	 * Delete this program and detach or destroy its shaders
	 */
	public void destroy(){
		vShader.detach(glId);
		fShader.detach(glId);
		glDeleteProgram(glId);
	}

	public void setParam(String name, int ... values){
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

	public void setParam(String name, float ... values){
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
