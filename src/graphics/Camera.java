package graphics;

import static org.lwjgl.opengl.GL11.glOrtho;

public class Camera {
	
	public static final double ZOOM_MIN = 0.3;
	public static final double ZOOM_MAX = 100.0;
	
	public double cx, cy;
	public double aspect_ratio;
	public double zoom;
	private double unit_height;
	
	public Camera(double x, double y, double w, double h){
		cx = x + w/2;
		cy = y + h/2;
		unit_height = h;
		aspect_ratio = w/h;
		zoom = 1.0;
	}
	
	public void shift(double dx, double dy){
		cx += dx;
		cy += dy;
	}
	
	public void zoom(double dz){
		zoom += dz;
		if(zoom < ZOOM_MIN) zoom = ZOOM_MIN;
		if(zoom > ZOOM_MAX) zoom = ZOOM_MAX;
	}
	
	public void glSetView(){
		double w = aspect_ratio * zoom * unit_height;
		double h = w / aspect_ratio;
		glOrtho(cx - w/2, cx + w/2, cy - h/2, cy + h/2, ZOOM_MIN, ZOOM_MAX);
	}
}
