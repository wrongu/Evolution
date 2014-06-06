package bio.organisms;

import environment.Environment;

public class Entity {
	
	protected double x;
	protected double y;
	protected Environment e;
	
	public Entity() {
		x = 0;
		y = 0;
		e = null;
	}
	
	public Entity(double x, double y, Environment e) {
		this.x = x;
		this.y = y;
		this.e = e;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}

}
