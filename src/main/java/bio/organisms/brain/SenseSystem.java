package bio.organisms.brain;

import environment.Environment;

public abstract class SenseSystem {
	
	protected Environment env;
	protected int sense_id;
	
	public SenseSystem(Environment e, int id){
		env = e;
		sense_id = id;
	}
	
	public abstract void senseAll();
}