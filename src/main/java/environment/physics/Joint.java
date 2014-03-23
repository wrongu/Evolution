package environment.physics;

import java.awt.Graphics2D;

import environment.Environment;

/**
 * One PointMass is the hub for multiple rods, but a joint only connects any two of those rods. 
 * So, a joint maintains a reference to the PointMass which it is associated with, and the two rods
 * that it is applying torques to. These rods are specified in order so that the a positive
 * joint angle is from A to B.
 */
public class Joint extends Structure {

	/** Torque exerted per unit muscle strength */
	public static final double MUSCLE_MULTIPLIER = 5.0;
	// setting torque per degree so that it is equal to Rod's spring strength for small displacements of
	// a rod with length 20.0
	public static final double PSEUDO_TORQUE_PER_RAD = 2;
	public static final double SPRING_FRICTION_CONSTANT = 0.01;
	public static final double ENERGY_PER_MUSCLE_STRENGTH = 0.01;

	private PointMass point;
	private Rod[] rods;
	// private double angle = 0;

	public Joint(double rest_angle, PointMass thePoint, Rod a, Rod b) {
		this(rest_angle, rest_angle, thePoint, a, b);
	}
	
	public Joint(double rest_angle1, double rest_angle2, PointMass thePoint, Rod a, Rod b) {
		super(rest_angle1, rest_angle2);
		point = thePoint;
		rods = new Rod[]{a, b};
		if(rods[0].getOtherEnd(point) == null || rods[1].getOtherEnd(point) == null) {
			System.out.println("You fucked up on a Joint constructor.");
			System.exit(0);
		}
		restValue1 %= 2*Math.PI;
		restValue2 %= 2*Math.PI;
		if(restValue1 < 0) { restValue1 += 2*Math.PI; }
		if(restValue2 < 0) { restValue2 += 2*Math.PI; }
		if(restValue2 < restValue1) { restValue2 += 2*Math.PI; }
	}

	@Override
	public double getMuscleMultiplier() {
		return Joint.MUSCLE_MULTIPLIER;
	}

	@Override
	public void physicsUpdate(Environment e) {
		double[] vA = pointMassDiffVector(point, rods[0].getOtherEnd(point));
		double[] vB = pointMassDiffVector(point, rods[1].getOtherEnd(point));

		double lenA = Math.sqrt(vA[0]*vA[0] + vA[1]*vA[1]);
		double lenB = Math.sqrt(vB[0]*vB[0] + vB[1]*vB[1]);

		if(lenA == 0 || lenB == 0)
			return;
		
		double uxa = vA[0]/lenA;
		double uya = vA[1]/lenA;
		double uxb = vB[0]/lenB;
		double uyb = vB[1]/lenB;
		
		// Calculate strain:
		double angle = getAngle(uxa, uya, uxb, uyb); // 0 < angle < 2PI
		double midAngle = 0.5*(restValue1 + restValue2);
		midAngle %= 2*Math.PI; // 0 < midAngle < 2PI
		double radAngle = 0.5*(restValue2 - restValue1);
		angle -= midAngle;
		if(angle > Math.PI) {angle -= 2*Math.PI;}
		if(angle < -Math.PI) {angle += 2*Math.PI;}
		double strain = 0;
		if(angle > radAngle) { strain = angle - radAngle; }
		if(angle < -radAngle) { strain = angle + radAngle; }
		
		double fax = -uya*PSEUDO_TORQUE_PER_RAD*strain;
		double fay = uxa*PSEUDO_TORQUE_PER_RAD*strain;
		double fbx = uyb*PSEUDO_TORQUE_PER_RAD*strain;
		double fby = -uxb*PSEUDO_TORQUE_PER_RAD*strain;
		
		rods[0].getOtherEnd(point).addForce(fax,fay);
		rods[1].getOtherEnd(point).addForce(fbx,fby);
		point.addForce(-fbx-fax,-fby-fay);
		
		// Calculate friction
		double vxa = rods[0].getOtherEnd(point).getVX() - point.getVX();
		double vya = rods[0].getOtherEnd(point).getVY() - point.getVY();
		double vxb = rods[1].getOtherEnd(point).getVX() - point.getVX();
		double vyb = rods[1].getOtherEnd(point).getVY() - point.getVY();
		
		double angularVel = (uxb*vyb - uyb*vxb) - (uxa*vya - uya*vxa);
		fax = SPRING_FRICTION_CONSTANT*uya*angularVel;
		fay = -SPRING_FRICTION_CONSTANT*uxa*angularVel;
		fbx = -SPRING_FRICTION_CONSTANT*uyb*angularVel;
		fby = SPRING_FRICTION_CONSTANT*uxb*angularVel;
		
		rods[0].getOtherEnd(point).addForce(-fax, -fay);
		rods[1].getOtherEnd(point).addForce(-fbx, -fby);
		point.addForce(fbx+fax,fby+fay);
		
		fax = -MUSCLE_MULTIPLIER*uya*muscleStrength/lenA;
		fay = MUSCLE_MULTIPLIER*uxa*muscleStrength/lenA;
		fbx = MUSCLE_MULTIPLIER*uyb*muscleStrength/lenB;
		fby = -MUSCLE_MULTIPLIER*uxb*muscleStrength/lenB;
		
		rods[0].getOtherEnd(point).addForce(-fax, -fay);
		rods[1].getOtherEnd(point).addForce(-fbx, -fby);
		point.addForce(fbx+fax,fby+fay);
		
		muscleStrength = 0;
	}

	private static double[] pointMassDiffVector(PointMass A, PointMass B){
		return new double[]{B.getPosX() - A.getPosX(), B.getPosY() - A.getPosY()};
	}

	public void draw(Graphics2D g, float shift, float shifty, float scalex, float scaley) {
		// currently, joints are not rendered as anything
	}
	
	// Returns the angle from (ax,ay) to (bx,by) about the origin.
	// Inputs need not be normalized.
	// Returns a value between 0 and 2*PI.
	private double getAngle(double ax, double ay, double bx, double by) {
		double ang1 = Math.atan2(ay, ax);
		double ang2 = Math.atan2(by, bx);
		if(ang2 < ang1) { ang2 += 2*Math.PI; }
		return ang2 - ang1;
	}
	
	@Override
	public double getEPMS() {return ENERGY_PER_MUSCLE_STRENGTH;}

	public void glDraw() {
		// opengl drawing
		// currently, joints are not rendered as anything
	}
}
