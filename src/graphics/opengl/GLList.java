package graphics.opengl;

import static org.lwjgl.opengl.GL11.*;

public class GLList {
	
	private int glId;
	
	private GLList(int id){
		glId = id;
	}
	
	public static GLList createList(){
		return createLists(1)[0];
	}
	
	public static GLList[] createLists(int n_lists){
		GLList[] ret = new GLList[n_lists];
		int id = glGenLists(n_lists);
		for(int i = 0; i < n_lists; i++)
			ret[i] = new GLList(id + i);
		return ret;
	}
	
	public void open(){
		glNewList(glId, GL_COMPILE);
	}
	
	public void close(){
		glEndList();
	}
	
	public void call(){
		glCallList(glId);
	}
	
	public void call(Program prog){
		prog.begin();
		call();
		prog.end();
	}
}
