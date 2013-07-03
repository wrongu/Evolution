package structure;

public abstract class Structure {
	
	public static final double ENERGY_PER_MUSCLE_STRENGTH = 0;
	protected double rest_value;
	protected double value;
	protected double value2;
	protected double health;
	
	public Structure(double init_value){
		rest_value = init_value;
		setValue(init_value);
		health = 1.0;
	}
	
	protected void setValue(double v){
		value = v;
		value2 = v*v;
	}
	
	protected double getValue(){
		return value;
	}
	
	protected double getValue2(){
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
	public abstract void forceConnectingStructures();
}
