package graphics.opengl;

/**
 * A BasicVertex contains the most common information associated with a single vertex: 3D position, 2D texture coordinates,
 * and an rgba color.
 * @author Richard
 *
 */
public class BasicVertex {
	private float[] coords;
	private float[] texCoords;
	private float[] rgba;
	
	public BasicVertex(float x, float y, float z, float s, float t, float r, float g, float b, float a){
		coords = new float[]{x, y, z};
		texCoords = new float[]{s, t};
		rgba = new float[]{r, g, b, a};
	}
	
	public float[] getPosition(){
		return coords;
	}
	
	public float[] getTexCoords(){
		return texCoords;
	}
	
	public float[] getColor(){
		return rgba;
	}
	
	public int getColorBytes(){
		int r = (int) (rgba[0] * 255f); r &= 0xFF;
		int g = (int) (rgba[1] * 255f); g &= 0xFF;
		int b = (int) (rgba[2] * 255f); b &= 0xFF;
		int a = (int) (rgba[3] * 255f); a &= 0xFF;
		return ((a << 24) | (b << 16) | (g << 8) | r);
	}
}
