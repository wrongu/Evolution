package graphics.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

public class Program {

	private int glId;
	private Shader vShader, fShader;
	private boolean is_being_used;

	private List<Integer> activeAttributes;

	private Program(int id, Shader vs, Shader fs){
		glId = id;
		vShader = vs;
		fShader = fs;
		is_being_used = false;
		activeAttributes = new LinkedList<Integer>();
	}

	public static Program createProgram(Shader vShader, Shader fShader){
		if(vShader == null || fShader == null) return null;

		int id = glCreateProgram();

		if(id == 0) return null;

		vShader.attach(id);
		fShader.attach(id);

		glLinkProgram(id);

		if(glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE){
			System.err.println("error linking program");

			int msglen = glGetProgrami(id, GL_INFO_LOG_LENGTH);
			String errmsg = glGetProgramInfoLog(id, msglen);
			System.err.println(errmsg);
			return null;
		}

		return new Program(id, vShader, fShader);
	}

	public static Program createProgram(String vSource, String fSource){
		return createProgram(
				Shader.fromSource(vSource, GL_VERTEX_SHADER),
				Shader.fromSource(fSource, GL_FRAGMENT_SHADER));
	}

	public void begin(){
		glUseProgram(glId);
		is_being_used = true;
	}

	public void end(){
		glUseProgram(0);
		is_being_used = false;
		unbindArrayAttribs();
	}

	/**
	 * Delete this program and detach or destroy its shaders
	 */
	public void destroy(){
		vShader.detach(glId);
		fShader.detach(glId);
		glDeleteProgram(glId);
	}

	/*
	 * FUNCTIONS FOR SETTING UNIFORMS
	 */

	public int getUniformLocation(String name){
		if(name == null) return -1;
		return glGetUniformLocation(glId, name);
	}

	public void setUniform(String name, int ... values){
		if(!is_being_used)
			System.err.println("cannot define uniform unless the program is being used");
		else if(values != null){
			int param = getUniformLocation(name);
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

	public void setUniform(String name, float ... values){
		if(!is_being_used)
			System.err.println("cannot define uniform unless the program is being used");
		else if(values != null){
			int param = getUniformLocation(name);
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

	public void setUniformMatrix(String name, FloatBuffer flatMatrix){
		if(!is_being_used)
			System.err.println("cannot define uniform matrix unless the program is being used");
		else{
			int param = getUniformLocation(name);
			switch(flatMatrix.capacity()){
			case 4:
				glUniformMatrix2(param, false, flatMatrix);
				break;
			case 9:
				glUniformMatrix3(param, false, flatMatrix);
				break;
			case 16:
				glUniformMatrix4(param, false, flatMatrix);
				break;
			default:
				System.err.println("setUniformMatrix was given a non-square matrix. size was "+flatMatrix.capacity());
				break;
			}

		}
	}

	/*
	 * FUNCTIONS FOR SETTING ATTRIBUTES (inputs)
	 */

	public int getAttributeLocation(String name){
		if(name == null) return -1;
		return glGetAttribLocation(glId, name);
	}

	public void setVertexArrayAttrib(String name, int size, int type, int stride, int offset) {
		setVertexArrayAttrib(getAttributeLocation(name), size, type, stride, offset);
	}

	public void setVertexArrayAttrib(int attribute, int size, int type, int stride, int offset){
		if(!is_being_used)
			System.err.println("cannot define attribute unless the program is being used");
		else{
			activeAttributes.add(attribute);
			glEnableVertexAttribArray(attribute);
			glVertexAttribPointer(attribute, size, type, false, stride, (long) offset);
		}
	}

	public void setVertexArrayAttribNormalize(int attribute, int size, int type, int stride, int offset){
		if(!is_being_used)
			System.err.println("cannot define attribute unless the program is being used");
		else{
			activeAttributes.add(attribute);
			glEnableVertexAttribArray(attribute);
			glVertexAttribPointer(attribute, size, type, true, stride, (long) offset);
		}
	}

	public void setVertexArrayAttrib(Attribute attr, int stride){
		activeAttributes.add(attr.location);
		if(attr.normalized)
			setVertexArrayAttribNormalize(attr.location, attr.numel, attr.type, stride, attr.vbo_offset);
		else
			setVertexArrayAttrib(attr.location, attr.numel, attr.type, stride, attr.vbo_offset);
	}

	public void unbindArrayAttribs(){
		for(int loc : activeAttributes) glDisableVertexAttribArray(loc);
	}
}
