package structure;

import physics.Structure;

/**
 * A muscle is basically a wrapper class around structures (i.e. joints and rods).
 * @author wrongu
 *
 */
public class Muscle {

	/** strength is like the attempted delta-value. 0 strength is "rest." large absolute value means exerting
	 * a lot of effort. */
	private double strength;
	private double maxStrength;
	
	/** each muscle acts to drive a structural element (A Rod or a Joint) */
	private Structure struct;
//	private Structure[] dependents;
	
	public Muscle(Structure s, double maxStr){
		struct = s;
		maxStrength = Math.max(0,maxStr);
	}
	
	public void setStrength(double strength){
		this.strength = strength;
		if(strength > maxStrength) { strength = maxStrength; }
		if(strength < maxStrength) { strength = -maxStrength; }
	}
	
	// Has the muscle exert a force on the structure, and returns the energy used.
	public double act() {
		return struct.exertMuscle(strength); // We could multiply by max strength to make strong muscles harder to get?
	}
}
