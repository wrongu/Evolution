package physics;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2d;

import environment.Environment;

public class PointMass {

	/** for physics, joints are modeled as point masses */
	public static final double DEFAULT_MASS = 1.0;
	public static final double VEL_MAX = 0.5;
	private double mass;
	private Vector2d pos;
	private Vector2d vel;
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
		double vmag = vel.length();
		if(vmag > VEL_MAX){
			vel.x *= VEL_MAX / vmag;
			vel.y *= VEL_MAX / vmag;
		}
		// apply point viscosity
		vel.x *= (1 - e.point_visc);
		vel.y *= (1 - e.point_visc);
		// move the point
		pos.x += dt * vel.x;
		pos.y += dt * vel.y;
		// newton's law for acceleration
		vel.x += dt * force.x / mass;
		vel.y += dt * force.y / mass;
		// reset forces to be summed for next update
		force.x = force.y = 0.0;
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
}
