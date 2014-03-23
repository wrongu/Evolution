package environment.physics;

import javax.vecmath.Vector2d;

public abstract class PhysicalObject {
	
	protected Vector2d pos;
	
	public PhysicalObject(double x, double y){
		pos = new Vector2d(x, y);
	}
	
	public PhysicalObject() {
		pos = new Vector2d();
	}
	
	public double getPosX() {
		return pos.x;
	}
	
	public double getPosY() {
		return pos.y;
	}
	
	public double[] getPos() {
		return new double[] {pos.x, pos.y};
	}
	
	public abstract void update(double dt);

}
