package environment.physics;

import java.awt.Graphics2D;
import javax.vecmath.Vector2d;
import environment.Environment;
import static org.lwjgl.opengl.GL11.*;

public class Rod extends Structure {
	
	/** change in value per unit strength */
	public static final double MUSCLE_MULTIPLIER = 1.0;
	public static final double ENERGY_PER_MUSCLE_STRENGTH = 0.01;
	
	/** FORCE_PER_DISPLACEMENT and SPRING_FRICTION_CONSTANT
	 *  should be set nearly equal and take values between 0.3 and 1.0. */
	public static final double FORCE_PER_DISPLACEMENT = 0.5;
	public static final double SPRING_FRICTION_CONSTANT = 0.6;
	
	private PointMass[] points;
	
	public Rod(double rest_length) {
		this(rest_length, rest_length);
	}
	
	public Rod(double rest_length1, double rest_length2){
		this(rest_length1, rest_length2, null, null);
	}
	
	public Rod(double rest_length1, double rest_length2, PointMass pm0, PointMass pm1) {
		super(rest_length1, rest_length2);
		if(restValue1 <= 0) {restValue1 = 0;}
		if(restValue2 <= 0) {restValue2 = 0;}
		if(restValue2 < restValue1) {
			double restHolder = restValue2;
			restValue2 = restValue1;
			restValue1 = restHolder;
		}
		
		points = new PointMass[]{pm0, pm1};
	}

	public void draw(Graphics2D g, float shiftx, float shifty, float scalex, float scaley) {
		PointMass j1 = points[0];
		PointMass j2 = points[1];
		if(!(j1 == null | j2 == null)){
			int x1 = (int) ((shiftx + j1.getPosX()) * scalex);
			int y1 = (int) ((shifty + j1.getPosY()) * scaley);
			int x2 = (int) ((shiftx + j2.getPosX()) * scalex);
			int y2 = (int) ((shifty + j2.getPosY()) * scaley);
			g.drawLine(x1, y1, x2, y2);
			//System.out.println("Draw rod ("+x1+", "+y1+"), ("+x2+", "+y2+")");
		}
	}

	public void glDraw() {
		// opengl render
		glColor4f(0f, 0.4f, 1.0f, 1.0f);
		glBegin(GL_LINES);
		{
			glVertex2d(points[0].getPosX(), points[0].getPosY());
			glVertex2d(points[1].getPosX(), points[1].getPosY());
		}
		glEnd();
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
					-Environment.VISCOSITY * proj1 * norm.x / norm.length(),
					-Environment.VISCOSITY * proj1 * norm.y / norm.length()
					);
			// second point
			double proj2 = norm.dot(mot2);
			points[1].addForce(
					-Environment.VISCOSITY * proj2 * norm.x / norm.length(),
					-Environment.VISCOSITY * proj2 * norm.y / norm.length()
					);
		}
	}

	public void addPoint(PointMass point) {
		if(points[0] == null) points[0] = point;
		else if(points[1] == null) points[1] = point;
		else throw new IllegalStateException("Rods cannot have more than 2 points");
	}
	
	public PointMass getOtherEnd(PointMass j){
		if(points[0] == j) return points[1];
		else if(points[1] == j) return points[0];
		else return null;
	}
	
	public double getActualLength(){
		double dx = points[0].getPosX() - points[1].getPosX();
		double dy = points[0].getPosY() - points[1].getPosY();
		
		return Math.sqrt(dx*dx + dy+dy);
	}
	
	public Vector2d asVector(){
		return new Vector2d(points[1].getPosX()-points[0].getPosX(), points[1].getPosY()-points[0].getPosY());
	}
	
	public double getAngleOffHorizontal(){
		if(points[0] != null && points[1] != null){
			return Math.atan2(points[1].getPosY()-points[0].getPosY(), points[1].getPosX()-points[0].getPosX());
		} else{
			return 0.0;
		}
	}

	@Override
	public void physicsUpdate(Environment e) {
		PointMass j1 = points[0];
		PointMass j2 = points[1];
		if(!(j1 == null | j2 == null)){
			double dx = j2.getPosX() - j1.getPosX();
			double dy = j2.getPosY() - j1.getPosY();
			double dist = Math.sqrt(dx*dx + dy*dy);
			
			// calculate the strain, which will be multiplied to calculate restoring force
			double strain = 0;
			if(dist < restValue1) strain = dist - restValue1;
			if(dist > restValue2) strain = dist - restValue2;
			
			// get unit vector
			double ux = dx;
			double uy = dy;
			if(dist != 0) {
				 ux /= dist;
				 uy /= dist;
			}
			
			// add forces to joints (point masses) relative to strain.
			j1.addForce(ux * strain * FORCE_PER_DISPLACEMENT, uy * strain * FORCE_PER_DISPLACEMENT);
			j2.addForce(-ux * strain * FORCE_PER_DISPLACEMENT, -uy * strain * FORCE_PER_DISPLACEMENT);

			// calculate friction and add it to point masses.
			double dvx = j2.getVX() - j1.getVX();
			double dvy = j2.getVY() - j1.getVY();
			double dvRadialComp = ux*dvx + uy*dvy;
			j2.addForce(-ux * dvRadialComp * SPRING_FRICTION_CONSTANT, -uy * dvRadialComp * SPRING_FRICTION_CONSTANT);
			j1.addForce(ux * dvRadialComp * SPRING_FRICTION_CONSTANT, uy * dvRadialComp * SPRING_FRICTION_CONSTANT);
			doViscosity(e);
			
			j1.addForce(ux * MUSCLE_MULTIPLIER * muscleStrength, uy * MUSCLE_MULTIPLIER * muscleStrength );
			j2.addForce(-ux * MUSCLE_MULTIPLIER * muscleStrength, -uy * MUSCLE_MULTIPLIER * muscleStrength );
			
			muscleStrength = 0;
		}
	}

	@Override
	public double getMuscleMultiplier() {
		return Rod.MUSCLE_MULTIPLIER;
	}
	
	@Override
	public double getEPMS() {return ENERGY_PER_MUSCLE_STRENGTH;}
	
	public Vector2d getMeanMotion(){
		return new Vector2d(
				(points[0].getVX()+points[1].getVX()) / 2,
				(points[0].getVY()+points[1].getVY()) / 2);
	}

	public PointMass getEnd(int i) {
		if(i < 0 || i > 1)
			return null;
		
		return points[i];
	}
	
	/**
	 * Displace the rod by 'displacement' at point 'position' along the rod.
	 * 
	 * @param displacement
	 * @param position
	 */
	public void displace(double dis, double vel, double t) {
		// Do positions
		double m0 = points[0].getMass();
		double m1 = points[1].getMass();
		double v0 = m1*(1-t)*dis/(m0*t*t + m1*(1-t)*(1-t));
		double v1 = m0*t*dis/(m0*t*t + m1*(1-t)*(1-t));
		// 0-1 and 1-0 is deliberate - perp([x,y]) = [-y,x]
		Vector2d norm = new Vector2d(points[0].getPosY()-points[1].getPosY(), points[1].getPosX()-points[0].getPosX());
		norm.normalize();
		points[0].addPos(norm.x*v0, norm.y*v0);
		points[1].addPos(norm.x*v1, norm.y*v1);
		
		// Do velocities
		v0 = m1*(1-t)*vel/(m0*t*t + m1*(1-t)*(1-t))*(1 + PointMass.ELASTICITY);
		v1 = m0*t*vel/(m0*t*t + m1*(1-t)*(1-t))*(1 + PointMass.ELASTICITY);
		points[0].addVel(norm.x*v0, norm.y*v0);
		points[1].addVel(norm.x*v1, norm.y*v1);
	}
	
}
