package environment.physics;

import environment.Environment;
import graphics.IDrawable;
import graphics.opengl.IDrawableGL;

public abstract class Structure implements IDrawable, IDrawableGL {
	
	/**
	 * Subclasses should override this constant if they require energy to move
	 */
//	public final double ENERGY_PER_MUSCLE_STRENGTH = 0;
	double restValue1;
	double restValue2;
//	public double value, value2;
	public double health;
	protected double muscleStrength;
	
	public Structure(double init_value1, double init_value2) {
		restValue1 = init_value1;
		restValue2 = init_value2;
//		setValue(init_value1);
		health = 1.0;
	}
	
	public Structure(double init_value){
		this(init_value, init_value);
	}
	
	public double getRestValue1() { return restValue1;}
	public double getRestValue2() { return restValue2;}
	
	public void setRestValue1(double val1) { restValue1 = val1; }
	public void setRestValue2(double val2) { restValue2 = val2; }
	public void setRestValues(double val1, double val2) { restValue1 = val1; restValue2 = val2; }
	
	public void addHealth(double deltaHealth){
		health += deltaHealth;
	}
	
	public boolean isAlive(){
		return health > 0.0;
	}
	
	public double exertMuscle(double strength) {
		muscleStrength += strength;
		return Math.abs(strength)*getEPMS();
	}
	
	public abstract double getMuscleMultiplier();
	public abstract void physicsUpdate(Environment e);
	public abstract double getEPMS();
}
