package structure;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.IDrawable;

public class Rod extends Structure implements IDrawable {
	
	/** change in value per strength */
	public static final double MUSCLE_MULTIPLIER = 5.0;
	public static final double ENERGY_PER_MUSCLE_STRENGTH = 1.0;
	public static final double SPRING_CONSTANT = 0.01;
	
	private Joint[] joints;
	
	public Rod(double rest_length){
		super(rest_length);
		joints = new Joint[2];
	}
	
	public void draw(Graphics2D g) {
		g.setColor(Color.WHITE);
		Joint j1 = joints[0];
		Joint j2 = joints[1];
		if(!(j1 == null | j2 == null))
			g.drawLine((int) j1.getX(), (int) j1.getY(), (int) j2.getX(), (int) j2.getY());
	}

	public void addJoint(Joint joint) {
		if(joints[0] == null) joints[0] = joint;
		else if(joints[1] == null) joints[1] = joint;
		else throw new IllegalStateException("Rods cannot have more than 2 Joints");
	}
	
	public Joint getOtherJoint(Joint j){
		if(joints[0] == j) return joints[1];
		else if(joints[1] == j) return joints[0];
		else return null;
	}

	@Override
	public void forceConnectingStructures() {
		Joint j1 = joints[0];
		Joint j2 = joints[1];
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
			j1.addForce(ux * strain * SPRING_CONSTANT, uy * strain * SPRING_CONSTANT);
			j2.addForce(-ux * strain * SPRING_CONSTANT, -uy * strain * SPRING_CONSTANT);
		}
	}

	@Override
	public double getMuscleMultiplier() {
		return Rod.MUSCLE_MULTIPLIER;
	}
}
