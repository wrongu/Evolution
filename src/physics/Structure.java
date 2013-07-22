package physics;

import environment.Environment;
import graphics.IDrawable;
import graphics.opengl.IDrawableGL;

public abstract class Structure implements IDrawable, IDrawableGL {
	
	/**
	 * Subclasses should override this constant if they require energy to move
	 */
//	public final double ENERGY_PER_MUSCLE_STRENGTH = 0;
	double restValue0;
	double restValue1;
	
	public Structure(double init_value0, double init_value1) {
		restValue0 = init_value0;
		restValue1 = init_value1;
	}
	
	public Structure(double init_value){
		this(init_value, init_value);
	}
	
	public double getRestValue0() { return restValue0;}
	public double getRestValue1() { return restValue1;}
	
	public void setRestValue1(double val0) { restValue0 = val0; }
	public void setRestValue2(double val1) { restValue1 = val1; }
	public void setRestValues(double val0, double val1) { restValue0 = val0; restValue1 = val1; }
	
//	public double exertMuscle(double strength) {
//		muscleStrength += strength;
//		return Math.abs(strength)*getEPMS();
//	}
	
//	public abstract double getMuscleMultiplier();
//	public abstract double getEPMS();
	
//	public void setIndex(int i) {index = i;}
//	public int getIndex() {return index; }
}
