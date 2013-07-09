package structure;
import javax.vecmath.Vector2d;


public class Util {
	public static double cross(Vector2d A, Vector2d B){
		return (A.x*B.y - B.x*A.y);
	}
}
