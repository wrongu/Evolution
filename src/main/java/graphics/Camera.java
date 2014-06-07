package graphics;

import static org.lwjgl.opengl.GL11.*;

public class Camera {
	
	public static final double ZOOM_MIN = 0.01;
	public static final double ZOOM_MAX = 100.0;
	
	public static final double SCROLL_EASE = 5.0;
	public static final double ZOOM_EASE = 3.0;
	
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
		zoom += dz;
		if(zoom < ZOOM_MIN) zoom = ZOOM_MIN;
		if(zoom > ZOOM_MAX) zoom = ZOOM_MAX;
	}
	
	public void glSetView(){
		ease_x += (x - ease_x) / SCROLL_EASE;
		ease_y += (y - ease_y) / SCROLL_EASE;
		ease_zoom += (zoom - ease_zoom) / ZOOM_EASE;
		glTranslated(ease_x, ease_y, 0.0);
		glScaled(ease_zoom, ease_zoom, 1.0);
	}

}
