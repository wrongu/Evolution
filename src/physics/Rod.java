package physics;

import java.awt.Graphics2D;
import javax.vecmath.Vector2d;
import environment.Environment;
import static org.lwjgl.opengl.GL11.*;

public class Rod extends Structure {
	
	/** change in value per unit strength */
	public static final double MUSCLE_MULTIPLIER = 1.0;
	public static final double ENERGY_PER_MUSCLE_STRENGTH = 0.01;
	public static final double FORCE_PER_DISPLACEMENT = 0.5;
	public static final double SPRING_FRICTION_CONSTANT = 0.5;
	
	public PointMass point0;
	public PointMass point1;
	
	public Rod(double rest_length) {
		this(rest_length, rest_length);
	}
	
	public Rod(double rest_length0, double rest_length1){
		this(rest_length0, rest_length1, new PointMass(1), new PointMass(1));
	}
	
	public Rod(double rest_length0, double rest_length1, PointMass pm0, PointMass pm1) {
		super(rest_length0, rest_length1);
		if(restValue0 <= 0) {restValue0 = 0;}
		if(restValue1 <= 0) {restValue1 = 0;}
		if(restValue1 < restValue0) {
			double restHolder = restValue1;
			restValue1 = restValue0;
			restValue0 = restHolder;
		}
		
		point0 = pm0;
		point1 = pm1;
	}

	public void draw(Graphics2D g, int shiftx, int shifty, double scalex, double scaley) {
//		PointMass j1 = points[0];
//		PointMass j2 = points[1];
//		if(!(j1 == null | j2 == null)){
//			int x1 = (int) ((shiftx + j1.getX()) * scalex);
//			int y1 = (int) ((shifty + j1.getY()) * scaley);
//			int x2 = (int) ((shiftx + j2.getX()) * scalex);
//			int y2 = (int) ((shifty + j2.getY()) * scaley);
//			g.drawLine(x1, y1, x2, y2);
//			//System.out.println("Draw rod ("+x1+", "+y1+"), ("+x2+", "+y2+")");
//		}
	}

	public void glDraw() {
		// opengl render
//		glColor4f(0f, 0.4f, 1.0f, 1.0f);
//		glBegin(GL_LINES);
//		{
//			glVertex2d(points[0].getX(), points[0].getY());
//			glVertex2d(points[1].getX(), points[1].getY());
//		}
//		glEnd();
	}
	
	/**
	 * apply viscosity to pointmasses connected to this rod.
	 * @param e the environment - specifies viscosity strength
	 */
//	public void doViscosity(Environment e){
//		Vector2d rod = PointMass.vecAB(points[0], points[1]);
//		if(rod.length() > 0){
//			Vector2d norm = new Vector2d(-rod.y, rod.x);
//			Vector2d mot1 = points[0].getVel();
//			Vector2d mot2 = points[1].getVel();
//			// first point
//			double proj1 = norm.dot(mot1);
//			points[0].addForce(
//					-e.viscosity * proj1 * norm.x / norm.length(),
//					-e.viscosity * proj1 * norm.y / norm.length()
//					);
//			// second point
//			double proj2 = norm.dot(mot2);
//			points[1].addForce(
//					-e.viscosity * proj2 * norm.x / norm.length(),
//					-e.viscosity * proj2 * norm.y / norm.length()
//					);
//		}
//	}

	public void addPoint(PointMass point) {
		if(point0 == null) point0 = point;
		else if(point1 == null) point1 = point;
		else throw new IllegalStateException("Rods cannot have more than 2 points");
	}
	
	public PointMass getOtherEnd(PointMass j){
		if(point0 == j) return point1;
		else if(point1 == j) return point0;
		else return null;
	}
	
//	public double getActualLength(){
//		double dx = points[0].getX() - points[1].getX();
//		double dy = points[0].getY() - points[1].getY();
//		
//		return Math.sqrt(dx*dx + dy+dy);
//	}

//	@Override
//	public void physicsUpdate(Environment e) {
//		PointMass j1 = points[0];
//		PointMass j2 = points[1];
//		if(!(j1 == null | j2 == null)){
//			double dx = j2.getX() - j1.getX();
//			double dy = j2.getY() - j1.getY();
//			double dist = Math.sqrt(dx*dx + dy*dy);
//			
//			// calculate the strain, which will be multiplied to calculate restoring force
//			double strain = 0;
//			if(dist < restValue1) strain = dist - restValue1;
//			if(dist > restValue2) strain = dist - restValue2;
//			
//			// get unit vector
//			double ux = dx;
//			double uy = dy;
//			if(dist != 0) {
//				 ux /= dist;
//				 uy /= dist;
//			}
//			
//			// add forces to joints (point masses) relative to strain.
//			j1.addForce(ux * strain * FORCE_PER_DISPLACEMENT, uy * strain * FORCE_PER_DISPLACEMENT);
//			j2.addForce(-ux * strain * FORCE_PER_DISPLACEMENT, -uy * strain * FORCE_PER_DISPLACEMENT);
//
//			// calculate friction and add it to point masses.
//			double dvx = j2.getVX() - j1.getVX();
//			double dvy = j2.getVY() - j1.getVY();
//			double dvRadialComp = ux*dvx + uy*dvy;
//			j2.addForce(-ux * dvRadialComp * SPRING_FRICTION_CONSTANT, -uy * dvRadialComp * SPRING_FRICTION_CONSTANT);
//			j1.addForce(ux * dvRadialComp * SPRING_FRICTION_CONSTANT, uy * dvRadialComp * SPRING_FRICTION_CONSTANT);
//			doViscosity(e);
//			
//			j1.addForce(ux * MUSCLE_MULTIPLIER * muscleStrength, uy * MUSCLE_MULTIPLIER * muscleStrength );
//			j2.addForce(-ux * MUSCLE_MULTIPLIER * muscleStrength, -uy * MUSCLE_MULTIPLIER * muscleStrength );
//			
//			muscleStrength = 0;
//		}
//	}

//	@Override
//	public double getMuscleMultiplier() {
//		return Rod.MUSCLE_MULTIPLIER;
//	}
//	
//	@Override
//	public double getEPMS() {return ENERGY_PER_MUSCLE_STRENGTH;}
	
	public PointMass getPoint0() { return point0; }
	public PointMass getPoint1() { return point1; }
	
//	public Vector2d getMeanMotion(){
//		return new Vector2d(
//				(points[0].getVX()+points[1].getVX()) / 2,
//				(points[0].getVY()+points[1].getVY()) / 2);
//	}
	
}
