package bio.organisms.brain;

import bio.organisms.AbstractOrganism;
import environment.Environment;

public interface ISense {

	public double doSense(Environment e, AbstractOrganism o);
	
}
