package physics;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.vecmath.Vector2d;


import environment.Environment;

import graphics.IDrawable;

public class Rod extends Structure {
	
	/** change in value per unit strength */
	public static final double MUSCLE_MULTIPLIER = 5.0;
	public static final double ENERGY_PER_MUSCLE_STRENGTH = 1.0;
	public static final double FORCE_PER_DISPLACEMENT = 0.1;
	
	private PointMass[] points;
	
	public Rod(double rest_length){
		super(rest_length);
		points = new PointMass[2];
	}
	
	public Rod(double rest_length, PointMass pm0, PointMass pm1) {
		this(rest_length);
		points = new PointMass[]{pm0, pm1};
	}

	public void draw(Graphics2D g, int shiftx, int shifty, double scalex, double scaley) {
		g.setColor(Color.WHITE);
		PointMass j1 = points[0];
		PointMass j2 = points[1];
		if(!(j1 == null | j2 == null)){
			int x1 = (int) ((shiftx + j1.getX()) * scalex);
			int y1 = (int) ((shifty + j1.getY()) * scaley);
			int x2 = (int) ((shiftx + j2.getX()) * scalex);
			int y2 = (int) ((shifty + j2.getY()) * scaley);
			g.drawLine(x1, y1, x2, y2);
			System.out.println("Draw rod ("+x1+", "+y1+"), ("+x2+", "+y2+")");
		}
	}
	
	/**
	 * apply viscosity to pointmasses connected to this rod.
	 * @param e the environment - specifies viscosity strength
	 */
	public void doViscosity(Environment e){
		Vector2d rod = PointMass.vecAB(points[0], points[1]);
		if(rod.length() > 0){
			Vector2d norm = new Vector2d(-rod.y, rod.x);
			Vector2d mot1 = points[0].getVel();
			Vector2d mot2 = points[1].getVel();
			// first point
			double proj1 = norm.dot(mot1);
			points[0].addForce(
					-e.viscosity * proj1 * norm.x / norm.length(),
					-e.viscosity * proj1 * norm.y / norm.length()
					);
			// second point
			double proj2 = norm.dot(mot2);
			points[1].addForce(
					-e.viscosity * proj2 * norm.x / norm.length(),
					-e.viscosity * proj2 * norm.y / norm.length()
					);
		}
	}

	public void addJoint(PointMass joint) {
		if(points[0] == null) points[0] = joint;
		else if(points[1] == null) points[1] = joint;
		else throw new IllegalStateException("Rods cannot have more than 2 Joints");
	}
	
	public PointMass getOtherEnd(PointMass j){
		if(points[0] == j) return points[1];
		else if(points[1] == j) return points[0];
		else return null;
	}
	
	public double getActualLength(){
		double dx = points[0].getX() - points[1].getX();
		double dy = points[0].getY() - points[1].getY();
		
		return Math.sqrt(dx*dx + dy+dy);
	}

	@Override
	public void physicsUpdate(Environment e) {
		PointMass j1 = points[0];
		PointMass j2 = points[1];
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
		doViscosity(e);
	}

	@Override
	public double getMuscleMultiplier() {
		return Rod.MUSCLE_MULTIPLIER;
	}
	
	public Vector2d getMeanMotion(){
		return new Vector2d(
				(points[0].getVX()+points[1].getVX()) / 2,
				(points[0].getVY()+points[1].getVY()) / 2);
	}

	public void draw() {
		// TODO Auto-generated method stub
		// opengl render
	}
}
