package structure;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.IDrawable;

public class Rod extends Structure implements IDrawable {
	
	/** change in value per unit strength */
	public static final double MUSCLE_MULTIPLIER = 5.0;
	public static final double ENERGY_PER_MUSCLE_STRENGTH = 1.0;
	public static final double FORCE_PER_DISPLACEMENT = 0.1;
	
	private PointMass[] joints;
	
	public Rod(double rest_length){
		super(rest_length);
		joints = new PointMass[2];
	}
	
	public Rod(double rest_length, PointMass pm0, PointMass pm1) {
		this(rest_length);
		joints = new PointMass[]{pm0, pm1};
	}

	public void draw(Graphics2D g, int shiftx, int shifty, double scalex, double scaley) {
		g.setColor(Color.WHITE);
		PointMass j1 = joints[0];
		PointMass j2 = joints[1];
		if(!(j1 == null | j2 == null)){
			int x1 = (int) ((shiftx + j1.getX()) * scalex);
			int y1 = (int) ((shifty + j1.getY()) * scaley);
			int x2 = (int) ((shiftx + j2.getX()) * scalex);
			int y2 = (int) ((shifty + j2.getY()) * scaley);
			g.drawLine(x1, y1, x2, y2);
		}
//		System.out.println("rod drawn: ("+(int) j1.getX()+","+(int) j1.getY()+") to ("
//				+(int) j2.getX()+","+(int) j2.getY()+")");
	}

	public void addJoint(PointMass joint) {
		if(joints[0] == null) joints[0] = joint;
		else if(joints[1] == null) joints[1] = joint;
		else throw new IllegalStateException("Rods cannot have more than 2 Joints");
	}
	
	public PointMass getOtherEnd(PointMass j){
		if(joints[0] == j) return joints[1];
		else if(joints[1] == j) return joints[0];
		else return null;
	}
	
	public double getActualLength(){
		double dx = joints[0].getX() - joints[1].getX();
		double dy = joints[0].getY() - joints[1].getY();
		
		return Math.sqrt(dx*dx + dy+dy);
	}

	@Override
	public void forceConnectingStructures() {
		PointMass j1 = joints[0];
		PointMass j2 = joints[1];
		if(!(j1 == null | j2 == null)){
			double dx = j2.getX() - j1.getX();
			double dy = j2.getY() - j1.getY();
			double dist = Math.sqrt(dx*dx + dy*dy);
			// spring force: f = -k*x, where k is the spring constant (or strength of muscle) and x is displacement from ideal length
			double strain = dist - this.getValue();
			// get unit vector
			double ux = dx / dist;
			double uy = dy / dist;
			// add forces to joints (point masses) relative to strain.
			j1.addForce(ux * strain * FORCE_PER_DISPLACEMENT, uy * strain * FORCE_PER_DISPLACEMENT);
			j2.addForce(-ux * strain * FORCE_PER_DISPLACEMENT, -uy * strain * FORCE_PER_DISPLACEMENT);
		}
	}

	@Override
	public double getMuscleMultiplier() {
		return Rod.MUSCLE_MULTIPLIER;
	}
}
