package graphics;

import static org.lwjgl.opengl.GL11.*;

public class Camera {
	
	public static final double ZOOM_MIN = 0.01;
	public static final double ZOOM_MAX = 100.0;
	
	private static final double ZOOM_EASE = 5.0;
	private static final double TRANSLATE_EASE = 5.0;
	
	public double x, y, x_target, y_target;
	public double zoom, zoom_target;
	
	public Camera(){
		x = y = x_target = y_target = 0.0;
		zoom = zoom_target = 1.0;
	}
	
	public void shift(double dx, double dy){
		x_target += dx;
		y_target += dy;
	}
	
	public void zoom(double dz){
		zoom_target += dz;
		if(zoom_target < ZOOM_MIN) zoom_target = ZOOM_MIN;
		if(zoom_target > ZOOM_MAX) zoom_target = ZOOM_MAX;
	}
	
	public void glSetView(){
		x += (x_target - x) / TRANSLATE_EASE;
		y += (y_target - y) / TRANSLATE_EASE;
		zoom += (zoom_target - zoom) / ZOOM_EASE;
		glTranslated(x, y, 0.0);
		glScaled(zoom, zoom, 1.0);
	}

}
