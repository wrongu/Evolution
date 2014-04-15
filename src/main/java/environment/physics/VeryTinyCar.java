package environment.physics;

import javax.vecmath.Vector2d;

public class VeryTinyCar extends PhysicalObject {
	
	public static final double DEFAULT_MASS = 1;
	
	private static final double FORWARD_RESISTANCE = 0.4;
	private static final double REVERSE_RESISTANCE = 5*FORWARD_RESISTANCE;
//	private static final double TURN_RESTORATION = 10;
	private static final double ADDFORCE_BUFFER = 0.00000000000001;// Prevents divide by zero errors in addForce. Must by > 0.
	
//	private Vector2d pos;
	private Vector2d dir;
	private double speed;
	private double acc; // tangential acceleration
	private double turn; // Is equal to 1/r where r = turn radius.
	private double last_turn; // tracks last frame's turn.
	private double mass;  // mass used in physics updates (f=ma)
	private double radius; // treated as a circle for collions
	
	public VeryTinyCar(double mass, double radius, double x, double y, double random) {
		super(x, y);
		random *= 2*Math.PI;
		this.dir = new Vector2d(Math.cos(random), Math.sin(random));
		this.mass = mass;
		this.radius = radius;
		speed = 0;
		acc = 0;
		turn = 0;
		last_turn = 0;
	}
	
	// Effectors
	public void addTurn(double dTurn) {
		this.turn += dTurn;
	}
	
	public void addThrust(double thrust) {
		this.speed += thrust/mass;
	}
	
	public void addForce(double[] force) {
		double tanAcc = (force[0]*dir.x + force[1]*dir.y)/mass;
		double nrmAcc = (force[0]*dir.y - force[1]*dir.x)/mass;
		
		acc += tanAcc;
		turn -= nrmAcc/(speed*speed + ADDFORCE_BUFFER);
	}
	
	public double getVelX() {
		return speed*dir.x;
	}
	
	public double getVelY() {
		return speed*dir.y;
	}

	public double[] getVel() {
		return new double[] {speed*dir.x, speed*dir.y};
	}
	
	public double getDirX() {
		return dir.x;
	}
	
	public double getDirY() {
		return dir.y;
	}
	
	public double[] getDir() {
		return new double[] {dir.x, dir.y};
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public double getTurn() {
		return last_turn;
	}

	public void update(double dt) {
		// Set last_turn
		last_turn = turn;
		
		// update speed
		speed += acc*dt;

		// update direction
		double distance = speed*dt;
		double sin = Math.sin(turn*distance);
		double cos = Math.cos(turn*distance);
		dir.set(cos*dir.x - sin*dir.y, sin*dir.x + cos*dir.y);
		dir.normalize(); // Could this possibly crash a long-running simulation?
		
		// update position
		pos.x += dir.x*distance;
		pos.y += dir.y*distance;
		
		// reset turn amount for next tick
		turn = 0;
		
		// apply resistance to speed
		speed -= (speed > 0) ? FORWARD_RESISTANCE*speed*dt : REVERSE_RESISTANCE*speed*dt;
	}

	public void collide(VeryTinyCar other) {
		double dx = other.getPosX() - this.getPosX();
		double dy = other.getPosY() - this.getPosY();
		double dist2 = (dx*dx + dy*dy);
		double overlap_dist2 = (other.radius+this.radius) * (other.radius+this.radius);
		if(dist2 < overlap_dist2){
			// TODO stage updates for the next frame so that behavior of a large cluster of colliding cars
			// doesn't depend on the order of updates
			// normalize collision direction
			double dist = Math.sqrt(dist2);
			dx = dist > 0.0 ? dx / dist : 1.;
			dy = dist > 0.0 ? dy / dist : 0.;
			double ratio = this.mass / (other.mass + this.mass);
			double ratio_inv = 1.0 - ratio;
			double overlap = Math.sqrt(overlap_dist2) - dist;
			// set positions such that they are no longer colliding
			pos.x += overlap * dx * ratio_inv;
			pos.y += overlap * dy * ratio_inv;
			other.pos.x -= overlap * dx * ratio;
			other.pos.y -= overlap * dy * ratio; 
			// compute the effect of the collision on cars' velocities
			double vx = dir.x*speed;
			double vy = dir.y*speed;
			double other_vx = other.dir.x*other.speed;
			double other_vy = other.dir.y*other.speed;
			double dvx = vx - other_vx;
			double dvy = vy - other_vy;
			double projdvx = (dvx*dx + dvy*dy)*dx;
			double projdvy = (dvx*dx + dvy*dy)*dy;
			// set velocities according to conservation of momentum
			this.dir.x = vx - projdvx * ratio_inv;
			this.dir.y = vy -  projdvy * ratio_inv;
			other.dir.x = other_vx +  projdvx * ratio;
			other.dir.y = other_vy +  projdvy * ratio;
			this.speed = this.dir.length();
			this.dir.normalize();
			other.speed = other.dir.length();
			other.dir.normalize();
		}
	}
}
