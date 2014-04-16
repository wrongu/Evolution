package graphics.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class Shader {
	
	private int glId;
	
	private Shader(int id){
		glId = id;
	}
	
	public static Shader fromSource(String source, int shaderType){
		int id = loadShader(source, shaderType);
		if(id == 0) return null;
		else return new Shader(id);
	}
	
	private static int loadShader(String source, int shaderType){
		try{
			// opengl has its own shader language. So, in another file (sepecified by 'source'),
			//	there is a program written in that language. We read it into a stringbuilder
			//	then build the shader and compile it from that stringbuilder.
			BufferedReader buff = new BufferedReader(new FileReader(source));
			StringBuilder builder = new StringBuilder();
			String line;
			while((line = buff.readLine()) != null)
				builder.append(line).append('\n');
			buff.close();
			// create the shader
			int shader = glCreateShader(shaderType);
			// bind the source to the shader and compile it
			glShaderSource(shader, builder);
			glCompileShader(shader);
			// check compilation
			if(glGetShader(shader, GL_COMPILE_STATUS) == GL_FALSE){
				System.err.println("error compiling shader from " + source);
				System.err.println(glGetShaderInfoLog(shader, 1024));
				glDeleteShader(shader);
				return 0;
			}
			System.out.println("loaded shader " + source);
			return shader;
		} catch(IOException e){
			System.err.println("could not open resource " + source);
			return 0;
		}
	}
	
	public int getId(){
		return glId;
	}
	
	public void destroy(){
		glDeleteShader(glId);
	}
}
