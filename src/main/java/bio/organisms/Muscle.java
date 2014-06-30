package bio.organisms;

import bio.organisms.brain.ActionSystem;
import environment.Environment;
import environment.physics.Structure;

/**
 * A muscle is basically a wrapper class around physics structures (i.e. joints and rods).
 * @author wrongu
 *
 */
public class Muscle extends ActionSystem{
	
	/** each muscle acts to drive a structural element (A Rod or a Joint) */
	private Structure struct;
//	private Structure[] dependents;
	
	public Muscle(AbstractOrganism o, Structure s, double strength){
		super(o, strength, "Muscle");
		struct = s;
	}
	
	@Override
	public void sub_act(double strength) {
		struct.exertMuscle(strength); // We could multiply by max strength to make strong muscles harder to get?
	}
}
