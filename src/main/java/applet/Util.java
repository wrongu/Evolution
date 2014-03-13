package applet;
import javax.vecmath.Vector2d;


public class Util {
	
	/**
	 * Perform cross product (A x B) of two Vector2d instances
	 *
	 * @return the value of the cross product
	 */
	public static double cross(Vector2d A, Vector2d B){
		return (A.x*B.y - B.x*A.y);
	}
	
	/**
	 * clamp radian value between 0 and 2PI
	 * @param r any radians
	 * @return the equivalent angle to r that is in [0, 2PI)
	 */
	public static double clamp_radians(double r){
		double two_pi = 2.0 * Math.PI;
		double full_loops = Math.floor(r / two_pi);
		return r - full_loops * two_pi;
	}
	
	public static void main(String[] args){
		// TESTS
		System.out.println("clamping radians:");
		
		for(int i=0; i<5; i++){
			double rad = 10 * (Math.random() - Math.random());
			System.out.println("["+rad+"] "+clamp_radians(rad));
		}
	}
}
