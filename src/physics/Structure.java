package physics;

import environment.Environment;
import graphics.IDrawable;
import graphics.opengl.IDrawableGL;

public abstract class Structure implements IDrawable, IDrawableGL {
	
	/**
	 * Subclasses should override this constant if they require energy to move
	 */
	public final double ENERGY_PER_MUSCLE_STRENGTH = 0;
	public double rest_value;
	public double value, value2;
	public double health;
	
	public Structure(double init_value){
		rest_value = init_value;
		setValue(init_value);
		health = 1.0;
	}
	
	public void setValue(double v){
		value = v;
		value2 = v*v;
	}
	
	public double getValue(){
		return value;
	}
	
	public double getValue2(){
		return value2;
	}
	
	public void addHealth(double deltaHealth){
		health += deltaHealth;
	}
	
	public boolean isAlive(){
		return health > 0.0;
	}
	
	public void actMuscle(double strength, Structure[] dependents){
		this.value = rest_value + strength * getMuscleMultiplier();
	}

	public abstract double getMuscleMultiplier();
	public abstract void physicsUpdate(Environment e);
}
