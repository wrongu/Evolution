package physics;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2d;

import environment.Environment;

public class PointMass {
	
	/** for physics, joints are modeled as point masses */
	public static final double DEFAULT_MASS = 1.0;
	private double mass;
	private Vector2d pos;
	private Vector2d vel;
	private Vector2d acc;
	private Vector2d force;
	
	private List<Rod> connections;
	
	public PointMass(double init_angle){
		this(DEFAULT_MASS, new Rod[]{});
	}
	
	public PointMass(double m, Rod ... rods){
		connections = new ArrayList<Rod>();
		for(Rod r : rods){
			connections.add(r);
			r.addJoint(this);
		}
		pos = new Vector2d();
		vel = new Vector2d();
		acc = new Vector2d();
		force = new Vector2d();
		mass = m;
	}
	
	public void initPosition(double x, double y){
		pos.x = x;
		pos.y = y;
	}
	
	public void addForce(double fx, double fy){
		force.x += fx;
		force.y += fy;
	}
	
	public void move(Environment e, double dt){
		// apply viscosity and friction
//		addForce(-vel.x * e.viscosity, -vel.y * e.viscosity);
//		double vmag = Math.sqrt(vel.x*vel.x + vel.y+vel.y);
//		addForce(-vel.x * e.friction / vmag, -vel.y * e.friction / vmag);
		
		// recover acceleration from mass and forces
		acc.x = force.x/mass;
		acc.y = force.y/mass;
		
		// move the point - acceleration needed for extreme forces
		pos.x += dt * vel.x + 0.5 * dt * dt * acc.x;
		pos.y += dt * vel.y + 0.5 * dt * dt * acc.y;
		
		// newton's law for acceleration
		vel.x += dt * acc.x;
		vel.y += dt * acc.y;
		
		// reset forces and acceleration to be summed for next update
		force.x = force.y = acc.x = acc.y = 0.0;
	}
	
	public Vector2d getPos() {
		return pos;
	}
	
	public double getX(){
		return pos.x;
	}
	
	public double getY(){
		return pos.y;
	}

	public void clearPhysics() {
		vel.x = vel.y = force.x = force.y = 0.0;
	}

	public double getVX() {
		return vel.x;
	}
	
	public double getVY() {
		return vel.y;
	}
	
	public Vector2d getVel(){
		return vel;
	}
	
	public static Vector2d vecAB(PointMass A, PointMass B){
		return new Vector2d(B.pos.x - A.pos.x, B.pos.y - A.pos.y);
	}

	public void addAcc(double fx, double fy) {
		force.x += fx/mass;
		force.y += fy/mass;
	}
}
