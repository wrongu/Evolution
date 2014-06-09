package graphics;

import static org.lwjgl.opengl.GL11.*;

public class Camera {
	
	public static final double ZOOM_MIN = 0.01;
	public static final double ZOOM_MAX = 100.0;
	
	public static final double EASE = 4.0;

	public double x, y;
	public double zoom;
	private double ease_x, ease_y, ease_zoom;
	
	public Camera(){
		x = y = 0.0;
		zoom = 1.0;
		ease_x = ease_y = 0.0;
		ease_zoom = 1.0;
	}
	
	public void shift(double dx, double dy){
		x += dx;
		y += dy;
	}
	
	public void zoom(double dz){
		double new_zoom = zoom * Math.exp(dz);
		if(new_zoom < ZOOM_MIN) new_zoom = ZOOM_MIN;
		if(new_zoom > ZOOM_MAX) new_zoom = ZOOM_MAX;
		double ratio = new_zoom / zoom;
		x *= ratio;
		y *= ratio;
		zoom = new_zoom;
	}
	
	public void glSetView(){
		ease_x += (x - ease_x) / EASE;
		ease_y += (y - ease_y) / EASE;
		ease_zoom += (zoom - ease_zoom) / EASE;
		glTranslated(ease_x, ease_y, 0.0);
		glScaled(ease_zoom, ease_zoom, 1.0);
	}

}
