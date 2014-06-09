package graphics;

import static org.lwjgl.opengl.GL11.*;

public class Camera {
	
	public static final double ZOOM_MIN = 0.01;
	public static final double ZOOM_MAX = 100.0;
	

	public static final double EASE = 4.0;

	public double x_target, y_target;
	public double zoom_target;
	private double x, y, zoom;
	
	public Camera(){
		x_target = y_target = 0.0;
		zoom_target = 1.0;
		x = y = 0.0;
		zoom = 1.0;
	}
	
	public void shift(double dx, double dy){
		x_target += dx;
		y_target += dy;
	}
	
	public void zoom(double dz){

		double new_zoom = zoom_target + dz;
		if(new_zoom < ZOOM_MIN) new_zoom = ZOOM_MIN;
		if(new_zoom > ZOOM_MAX) new_zoom = ZOOM_MAX;
		double ratio = new_zoom / zoom_target;
		x_target *= ratio;
		y_target *= ratio;
		zoom_target = new_zoom;
	}
	
	public void glSetView(){
		x += (x_target - x) / EASE;
		y += (y_target - y) / EASE;
		zoom += (zoom_target - zoom) / EASE;
		glTranslated(x, y, 0.0);
		glScaled(zoom, zoom, 1.0);
	}

}
