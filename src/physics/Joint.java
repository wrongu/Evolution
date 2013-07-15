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
	public static final double TORQUE_PER_RAD = Rod.FORCE_PER_DISPLACEMENT * 20.0*50;
	public static final double ENERGY_PER_MUSCLE_STRENGTH = 1.0;
	public static final double SPRING_FRICTION_CONSTANT = 1;

	private PointMass point;
	private Rod[] rods;
	// private double angle = 0;

	public Joint(double rest_angle, PointMass thePoint, Rod a, Rod b) {
		super(rest_angle);
		point = thePoint;
		rods = new Rod[]{a, b};
		if(rods[0].getOtherEnd(point) == null || rods[1].getOtherEnd(point) == null) {
			System.out.println("You fucked up on a Joint constructor.");
			System.exit(0);
		}
		// angle = rodAngle();
	}

	@Override
	public double getMuscleMultiplier() {
		return Joint.MUSCLE_MULTIPLIER;
	}

	/*
	@Override
	public void physicsUpdate(Environment e) {
		// strain is the difference between where this joint is and where we want it to be
		// so, positive strain means the angle AB is too wide. So, A should be pushed CCW
		// and B should be pushed CW
		rodAngle();
		double strain = angle - getValue();

		double[] vA = pointMassDiffVector(this.point, rods[0].getOtherEnd(this.point));
		double[] vB = pointMassDiffVector(this.point, rods[0].getOtherEnd(this.point));

		double lenA = Math.sqrt(vA[0]*vA[0] + vA[1]*vA[1]);
		double lenB = Math.sqrt(vB[0]*vB[0] + vB[1]*vB[1]);

		double uxa = vA[0];
		double uya = vA[1];
		double uxb = vB[0];
		double uyb = vB[1];
		if(lenA != 0) {
			uxa /= lenA;
			uya /= lenA;
		}
		if(lenB != 0) {
			uxb /= lenB;
			uyb /= lenB;
		}
		
		rods[0].getOtherEnd(this.point).addForce(-TORQUE_PER_RAD*uya*strain/lenA, TORQUE_PER_RAD*uxa*strain/lenA);
		rods[1].getOtherEnd(this.point).addForce(TORQUE_PER_RAD*uyb*strain/lenB, -TORQUE_PER_RAD*uxb*strain/lenB);
		
		double vxa = rods[0].getOtherEnd(this.point).getVX() - this.point.getVX();
		double vya = rods[0].getOtherEnd(this.point).getVY() - this.point.getVY();
		double vxb = rods[1].getOtherEnd(this.point).getVX() - this.point.getVX();
		double vyb = rods[1].getOtherEnd(this.point).getVY() - this.point.getVY();
		
		double angVel = (uxb*vyb - uyb*vxb) - (uxa*vya - uya*vxa);
		
		rods[0].getOtherEnd(this.point).addForce(-SPRING_FRICTION_CONSTANT*uya*angVel/lenA, SPRING_FRICTION_CONSTANT*uxa*angVel/lenA);
		rods[1].getOtherEnd(this.point).addForce(SPRING_FRICTION_CONSTANT*uyb*angVel/lenB, -SPRING_FRICTION_CONSTANT*uxb*angVel/lenB);
	}
	*/

	@Override
	public void physicsUpdate(Environment e) {
		double[] vA = pointMassDiffVector(point, rods[0].getOtherEnd(point));
		double[] vB = pointMassDiffVector(point, rods[0].getOtherEnd(point));

		double lenA = Math.sqrt(vA[0]*vA[0] + vA[1]*vA[1]);
		double lenB = Math.sqrt(vB[0]*vB[0] + vB[1]*vB[1]);

		if(lenA == 0 || lenB == 0)
			return;
		
		double uxa = vA[0]/lenA;
		double uya = vA[1]/lenA;
		double uxb = vB[0]/lenB;
		double uyb = vB[1]/lenB;
		
		// sin(a - b) = sin(a)cos(b) - cos(a)sin(b)
		double sinStrain = (uxa*uyb - uya*uxb)*Math.cos(getValue()) - (uxa*uxb + uya*uyb)*Math.sin(getValue());
		double fax = -uya*TORQUE_PER_RAD*sinStrain/lenA;
		double fay = uxa*TORQUE_PER_RAD*sinStrain/lenA;
		double fbx = uyb*TORQUE_PER_RAD*sinStrain/lenB;
		double fby = -uxb*TORQUE_PER_RAD*sinStrain/lenB;
		
		rods[0].getOtherEnd(point).addForce(fax,fay);
		rods[1].getOtherEnd(point).addForce(fbx,fby);
		point.addForce(-fbx-fax,-fby-fay);
		
		// Calculate 
		
		double vxa = rods[0].getOtherEnd(this.point).getVX() - this.point.getVX();
		double vya = rods[0].getOtherEnd(this.point).getVY() - this.point.getVY();
		double vxb = rods[1].getOtherEnd(this.point).getVX() - this.point.getVX();
		double vyb = rods[1].getOtherEnd(this.point).getVY() - this.point.getVY();
		
		double angVel = (uxb*vyb - uyb*vxb) - (uxa*vya - uya*vxa);
		fax = -SPRING_FRICTION_CONSTANT*uya*angVel/lenA;
		fay = SPRING_FRICTION_CONSTANT*uxa*angVel/lenA;
		fbx = SPRING_FRICTION_CONSTANT*uyb*angVel/lenB;
		fby = -SPRING_FRICTION_CONSTANT*uxb*angVel/lenB;
		
		rods[0].getOtherEnd(this.point).addForce(fax, fay);
		rods[1].getOtherEnd(this.point).addForce(fbx, fby);
		point.addForce(-fbx-fax,fby-fay);
	}
	
	// why all the "pivot = this.point;"? Why not just use point instead of pivot?
	/*
	private double rodAngle(){
		PointMass pivot = this.point;
		PointMass pmA = rods[0].getOtherEnd(pivot);
		PointMass pmB = rods[1].getOtherEnd(pivot);

		double ax = pmA.getX() - pivot.getX();
		double ay = pmA.getY() - pivot.getY();
		double adist = Math.sqrt(ax*ax + ay*ay);
		if(adist != 0) {
			ax /= adist;
			ay /= adist;
		} else {
			ax = 1;
			ay = 0;
		}
		// double rota = Math.atan2(ay, ax);

		double bx = pmB.getX() - pivot.getX();
		double by = pmB.getY() - pivot.getY();
		double bdist = Math.sqrt(bx*bx + by*by);
		if(bdist != 0) {
			bx /= bdist;
			by /= bdist;
		} else {
			bx = 1;
			by = 0;
		}

		// double c = Math.cos(-rota);
		// double s = Math.sin(-rota);

		// TODO - make everything in radians? BAM!
		double ang = Math.atan2(ax*bx + ay*by, -ay*bx + ax*by);
		return angle += ang - (angle % 2*Math.PI);
	}
	*/
	

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
