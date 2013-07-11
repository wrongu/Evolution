package structure;

import physics.Structure;

/**
 * A muscle is basically a wrapper class around structures (i.e. joints and rods). Every structure has some "zero value" (rod length
 * or joint angle) that a Muscle acts on.
 * @author Richard
 *
 */
public class Muscle {

	/** strength is like the attempted delta-value. 0 strength is "rest." large absolute value means exerting
	 * a lot of effort. */
	private double strength;
	
	/** each muscle acts to drive a structural element (A Rod or a Joint) */
	private Structure struct;
	private Structure[] dependents;
	
	public Muscle(Structure s, Structure... dependents){
		struct = s;
		this.dependents = dependents;
		strength = 0.0;
	}
	
	/**
	 * Enact whatever movement this class is capable of
	 * @param target the destination value. For Links, this is length, for joints it is the angle off of 'zero'.
	 * @param strength the force to apply. 0 means fully relaxed, 1 is full force. 
	 */
	public void setStrength(double strength){
		this.strength = strength;
	}
	
	public void act(Organism host){
		double energy = host.requestEnergy(strength * struct.ENERGY_PER_MUSCLE_STRENGTH);
		struct.actMuscle(energy / struct.ENERGY_PER_MUSCLE_STRENGTH, dependents);
	}
}
