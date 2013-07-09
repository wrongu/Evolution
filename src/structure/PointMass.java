package structure;

import java.util.ArrayList;
import java.util.List;

import environment.Environment;

public class PointMass {
	
	/** for physics, joints are modeled as point masses */
	public static final double DEFAULT_MASS = 1.0;
	private double mass;
	private double x, y;
	private double vx, vy;
	private double fx, fy;
	
	private List<Rod> connections;
	
	public PointMass(double init_angle){
		this(new Rod[]{});
	}
	
	public PointMass(Rod ... rods){
		connections = new ArrayList<Rod>();
		for(Rod r : rods){
			connections.add(r);
			r.addJoint(this);
		}
		vx = vy = fx = fy = 0.0;
		mass = DEFAULT_MASS;
	}
	
	public void initPosition(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public void addForce(double fx, double fy){
		this.fx += fx;
		this.fy += fy;
	}
	
	public void move(Environment e, double dt){
		// apply viscosity and friction
//		addForce(-vx * e.viscosity, -vy * e.viscosity);
//		double vmag = Math.sqrt(vx*vx + vy+vy);
//		addForce(-vx * e.friction / vmag, -vy * e.friction / vmag);
		// move the point
		x += dt * vx;
		y += dt * vy;
		// newton's law for acceleration
		vx += dt * fx / mass;
		vy += dt * fy / mass;
		// reset forces to be summed for next update
		fx = fy = 0.0;
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}

	public void clearPhysics() {
		vx = vy = fx = fy = 0.0;
	}

	public double getVX() {
		return vx;
	}
	public double getVY() {
		return vy;
	}
}
