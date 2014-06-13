package utils;

import org.lwjgl.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.util.glu.GLU.*;
import static org.lwjgl.BufferUtils.*;
 
public class GLTest {
  public static void main(String[] args) {
    PixelFormat pixelFormat = new PixelFormat();
 
    ContextAttribs contextAttributes = new ContextAttribs(3, 2);
    contextAttributes.withForwardCompatible(true);
    contextAttributes.withProfileCore(true);
 
    try {
		Display.setDisplayMode(new DisplayMode(800,600));
	    Display.create(pixelFormat, contextAttributes);
	} catch (LWJGLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 
    System.out.println("OS name " + System.getProperty("os.name"));
    System.out.println("OS version " + System.getProperty("os.version"));
    System.out.println("LWJGL version " + org.lwjgl.Sys.getVersion());
    System.out.println("OpenGL version " + glGetString(GL_VERSION));
 
    // On my laptop, this outputs:
    //
    //   OS name Mac OS X
    //   OS version 10.7.4
    //   LWJGL version 2.8.4
    //   OpenGL version 2.1 NVIDIA-7.18.18
    //
    // Later on I use GL30.glGenVertexArrays and I get a 
    // "Function is not supported" exception.
    //
    // I assume this is due to the OptnGL version.
    //
    // From what I can tell, LWJGL 2.8.0+ should try to select OptnGL 3.2.
    // This doesn't appear to be the case (or maybe I'm not getting something).
  }
}