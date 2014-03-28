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
	private double mass;
	
	public VeryTinyCar(double mass, double x, double y, double random) {
		super(x, y);
		random *= 2*Math.PI;
		this.dir = new Vector2d(Math.cos(random), Math.sin(random));
		this.mass = mass;
		speed = 0;
		acc = 0;
		turn = 0;
		last_turn = 0;
	}
	
//	public void initVel(double vel_x, double vel_y) {
//		speed = Math.hypot(vel_x, vel_y);
//		if(speed > 0) {
//			dir.x = vel_x/speed;
//			dir.y = vel_y/speed;
//			dir.normalize(); // Possibly cause problems in long-running simulations? Not with speed cap.
//		}
//	}
	
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
	
	// Gratuitous amounts of getters	
//	public double getPosX() {
//		return pos.x;
//	}
//	
//	public double getPosY() {
//		return pos.y;
//	}
//	
//	public double[] getPos() {
//		return new double[] {pos.x, pos.y};
//	}
	
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
		
//		// apply restoring force to turn
//		turn -= TURN_RESTORATION*turn*dt;
		turn = 0;
		
		// apply resistance to speed
		speed -= (speed > 0) ? FORWARD_RESISTANCE*speed*dt : REVERSE_RESISTANCE*speed*dt;
	}

}
