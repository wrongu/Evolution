package graphics;

import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Matrix4f;

public class Camera {
	
	public static final float ZOOM_MIN = 0.01f; // zoomed out
	public static final float ZOOM_MAX = 10.0f; // zoomed in
	public static final double EASE = 4.0; // lower=>jerkier; higher=>laggy

	/** x-coordinate in world-space of the center (focus) of the camera */
	private float x;
	/** y-coordinate in world-space of the center (focus) of the camera */
	private float y;
	/** pixels per world-space distance */
	private float zoom;
	public float x_target, y_target;
	public float zoom_target;
	
	public Camera(){
		x_target = y_target = 0.0f;
		zoom_target = 1.0f;
		x = y = 0.0f;
		zoom = 1.0f;
	}
	
	public void shift(double dx, double dy){
		x_target += dx;
		y_target += dy;
	}
	
	public void zoom(double dz){
		// zoom and check bounds
		double new_zoom = zoom_target + dz;
		if(new_zoom < ZOOM_MIN) new_zoom = ZOOM_MIN;
		if(new_zoom > ZOOM_MAX) new_zoom = ZOOM_MAX;
		// because scale is applied after translation,
		// we need to undo this effect with extra translation
		double ratio = new_zoom / zoom_target;
		x_target /= ratio;
		y_target /= ratio;
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
		float half_width = viewport_width / 2f;
		float half_height = viewport_height / 2f;
		return new float[]{
				// x, y are in world-space
				// zoom is pix/world-space
				// width/height are in pix
				x - half_width  / zoom,
				y - half_height / zoom,
				x + half_width  / zoom,
				y + half_height / zoom
		};
	}

	public Matrix4f projection(float viewport_width, float viewport_height){
		// construct orthogonal matrix
		float x_orth = 2f / viewport_width;
		float y_orth = 2f / viewport_height;
		
		Matrix4f matrix = new Matrix4f();
		Matrix4f.setIdentity(matrix);
		matrix.m00 = x_orth / zoom;
		matrix.m11 = y_orth / zoom;
		matrix.m30 = -x;
		matrix.m31 = -y;
		return matrix;
	}
}
