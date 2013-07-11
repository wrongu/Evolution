package physics;

import java.awt.Graphics2D;

import environment.Environment;

/**
 * One PointMass is the hub for multiple rods, but a joint only connects any two of those rods. 
 * So, a joint maintains a reference to the PointMass which it is associated with, and the two rods
 * that it is applying torques to. These rods are specified in order so that the a positive
 * joint angle is from A to B.
 */
public class Joint extends Structure {

	/** degrees joints rotate per unit muscle strength */
	public static final double MUSCLE_MULTIPLIER = 30.0;
	// setting torque per degree so that it is equal to Rod's spring strength for small displacements of
	// a rod with length 20.0
	public static final double TORQUE_PER_DEGREE = Rod.FORCE_PER_DISPLACEMENT * 20.0 * Math.PI / 180;
	public static final double ENERGY_PER_MUSCLE_STRENGTH = 1.0;

	private PointMass point;
	private Rod[] rods;

	public Joint(double rest_angle, PointMass thePoint, Rod a, Rod b) {
		super(rest_angle);
		point = thePoint;
		rods = new Rod[]{a, b};
	}

	@Override
	public double getMuscleMultiplier() {
		return Joint.MUSCLE_MULTIPLIER;
	}

	@Override
	public void physicsUpdate(Environment e) {
		// strain is the difference between where this joint is and where we want it to be
		// so, positive strain means the angle AB is too wide. So, A should be pushed CCW
		// and B should be pushed CW
		double strain = rodAngle() - getValue();

		double[] vA = pointMassDiffVector(this.point, rods[0].getOtherEnd(this.point));
		double[] vB = pointMassDiffVector(this.point, rods[0].getOtherEnd(this.point));

		double lenA = Math.sqrt(vA[0]*vA[0] + vA[1]*vA[1]);
		double lenB = Math.sqrt(vB[0]*vB[0] + vB[1]*vB[1]);

		double uxa = vA[0] / lenA;
		double uya = vA[1] / lenA;
		double uxb = vB[0] / lenB;
		double uyb = vB[1] / lenB;

		rods[0].getOtherEnd(this.point).addForce(-uya*strain/lenA, uxa*strain/lenA);
		rods[1].getOtherEnd(this.point).addForce(uyb*strain/lenB, -uxb*strain/lenB);
	}

	private double rodAngle(){
		PointMass pivot = this.point;
		PointMass pmA = rods[0].getOtherEnd(pivot);
		PointMass pmB = rods[1].getOtherEnd(pivot);

		double ax = pmA.getX() - pivot.getX();
		double ay = pmA.getY() - pivot.getY();
		double rota = Math.atan2(ay, ax);

		double bx = pmB.getX() - pivot.getX();
		double by = pmB.getY() - pivot.getY();

		double c = Math.cos(-rota);
		double s = Math.sin(-rota);

		// TODO - make everything in radians?
		return Math.atan2(c*bx + s*by, -s*bx + c*by) * 180.0 / Math.PI;
	}

	private static double[] pointMassDiffVector(PointMass A, PointMass B){
		return new double[]{B.getX() - A.getX(), B.getY() - A.getY()};
	}

	public void draw(Graphics2D g, int shift, int shifty, double scalex, double scaley) {
		// currently, joints are not rendered as anything
	}

	public void draw() {
		// opengl drawing
		// currently, joints are not rendered as anything
	}
}
