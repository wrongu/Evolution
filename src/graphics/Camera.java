package graphics;

import static org.lwjgl.opengl.GL11.*;

public class Camera {
	
	public static final double ZOOM_MIN = 0.01;
	public static final double ZOOM_MAX = 100.0;
	
	public double x, y;
	public double zoom;
	
	public Camera(){
		x = y = 0.0;
		zoom = 1.0;
	}
	
	public void shift(double dx, double dy){
		x += dx;
		y += dy;
	}
	
	public void zoom(double dz){
		zoom += dz;
		if(zoom < ZOOM_MIN) zoom = ZOOM_MIN;
		if(zoom > ZOOM_MAX) zoom = ZOOM_MAX;
	}
	
	public void glSetView(){
		glTranslated(x, y, 0.0);
		glScaled(zoom, zoom, 1.0);
	}
}
