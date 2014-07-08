package graphics.opengl;

import org.lwjgl.util.vector.Matrix4f;

public class Camera {
	
	public static final float ZOOM_MIN = 0.1f; // zoomed out
	public static final float ZOOM_MAX = 10.0f; // zoomed in
	public static final double EASE = 8.0; // lower=>jerkier; higher=>laggy

	/** x-coordinate in world-space of the center (focus) of the camera */
	private float x;
	/** y-coordinate in world-space of the center (focus) of the camera */
	private float y;
	/** pixels per world-space distance */
	private float zoom;
	public float x_target, y_target;
	public float zoom_target;
	
	public Camera(){
		x_target = y_target = x = y = 0.0f;
		zoom_target = zoom = 1.0f;
	}
	
	public void shift(float dx, float dy){
		x_target += dx;
		y_target += dy;
	}
	
	public void shiftClipSpace(float dx, float dy){
		x_target += dx / zoom;
		y_target += dy / zoom;
	}
	
	public void zoom(double dz){
		// zoom and check bounds
		double new_zoom = zoom_target * (1.0 - dz);
		if(new_zoom < ZOOM_MIN) new_zoom = ZOOM_MIN;
		if(new_zoom > ZOOM_MAX) new_zoom = ZOOM_MAX;
		zoom_target = (float) new_zoom;
	}
	
	/**
	 * smoothly animate towards target position and zoom
	 */
	public void ease(){
		x += (x_target - x) / EASE;
		y += (y_target - y) / EASE;
		zoom += (zoom_target - zoom) / EASE;
	}
	
	/**
	 * @return [xmin ymin xmax ymax] of visible space
	 */
	public float[] getWorldBounds(float viewport_width, float viewport_height){
		float xmin = x - viewport_width / (2f * zoom);
		float xmax = x + viewport_width / (2f * zoom);
		float ymin = y - viewport_height / (2f * zoom);
		float ymax = y + viewport_height / (2f * zoom);
		return new float[]{ xmin, ymin, xmax, ymax };
	}
	
	public float[] getWorldBoundsBuffer(float viewport_width, float viewport_height, float buffer){
		// extends the range of getWorldBounds() by 'buffer' in each direction. buffer is in world-dimensions
		return getWorldBounds(viewport_width + 2f * zoom * buffer, viewport_height + 2f * zoom * buffer);
	}

	public Matrix4f projection(float viewport_width, float viewport_height){
		// construct a 4x4 matrix such that a vector xyzw (xy world space, z ignored, w=1),
		// multiplied by this matrix, is projected to clip-space (-1,-1):(1,1)
		Matrix4f matrix = new Matrix4f();
		Matrix4f.setZero(matrix);
		
		matrix.m00 = 2f * zoom / viewport_width;
		matrix.m11 = 2f * zoom / viewport_height;
		matrix.m22 = 1f;
		matrix.m33 = 1f;
		matrix.m30 = -2f * zoom * x / viewport_width;
		matrix.m31 = -2f * zoom * y / viewport_height;
		
		return matrix;
	}
	
	public Matrix4f inverse_projection(float viewport_width, float viewport_height){
		Matrix4f matrix = new Matrix4f();
		Matrix4f.setZero(matrix);
		
		matrix.m00 = viewport_width / (2f * zoom);
		matrix.m11 = viewport_height / (2f * zoom);
		matrix.m22 = 1f;
		matrix.m33 = 1f;
		matrix.m30 = x;
		matrix.m31 = y;
		
		return matrix;
		
//		Matrix4f proj = this.projection(viewport_width, viewport_height);
//		return (Matrix4f) proj.invert();
	}
}
