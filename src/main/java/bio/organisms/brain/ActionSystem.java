package bio.organisms.brain;

import environment.Environment;

public abstract class ActionSystem {
	
	protected Environment env;
	protected int output_id;
	
	public double POWER_PER_BRAIN_OUTPUT;
	public double STRENGTH_PER_BRAIN_OUTPUT;
	
	public ActionSystem(Environment e, int id, double power, double strength){
		env = e;
		output_id = id;
		POWER_PER_BRAIN_OUTPUT = power;
		STRENGTH_PER_BRAIN_OUTPUT = strength;
	}
	
	public abstract void performAll(double dt);
}