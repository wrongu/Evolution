package structure;

import java.util.ArrayList;
import java.util.List;

import environment.Environment;

public class Joint extends Structure {
	
	/** degrees joints rotate per unit muscle strength */
	public static final double MUSCLE_MULTIPLIER = 30.0;
	public static final double ENERGY_PER_MUSCLE_STRENGTH = 1.0;
	/** for physics, joints are modeled as point masses */
	public static final double JOINT_MASS = 1.0;
	private double x, y;
	private double vx, vy;
	private double fx, fy;
	
	private List<Rod> connections;
	
	public Joint(double init_angle){
		this(init_angle, new Rod[0]);
		connections = new ArrayList<Rod>();
	}
	
	public Joint(double init_angle, Rod ... rods){
		super(init_angle);
		connections = new ArrayList<Rod>();
		for(Rod r : rods){
			connections.add(r);
			r.addJoint(this);
		}
		vx = vy = fx = fy = 0.0;
	}
	
	public void initPosition(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public void addForce(double fx, double fy){
		this.fx += fx;
		this.fy += fy;
	}
	
	public void move(Environment e){
		// apply viscosity
		addForce(-vx * e.viscosity, -vy * e.viscosity);
		// move the point
		x += vx;
		y += vy;
		// newton's law for acceleration
		vx += fx / JOINT_MASS;
		vy += fy / JOINT_MASS;
		// reset forces to be summed for next update
		fx = fy = 0.0;
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}

	@Override
	public void forceConnectingStructures() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getMuscleMultiplier() {
		return Joint.MUSCLE_MULTIPLIER;
	}
}
