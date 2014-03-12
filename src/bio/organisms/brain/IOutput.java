package bio.organisms.brain;

import bio.organisms.AbstractOrganism;

public abstract class IOutput {
	
	protected AbstractOrganism theOrganism;
	private double multiplier;
	
	public IOutput(AbstractOrganism o, double strength){
		this.theOrganism = o;
		this.multiplier = strength;
	}
	
	/**
	 * A wrapper for subclass Outputs that ensures the organism uses energy
	 * @param energy requested energy
	 * @param o the parent organism
	 */
	public final void act(double energy){
		double request = energy * this.multiplier;
		double e = theOrganism.useEnergy(request);
		this.sub_act(e);
	}
	
	protected abstract void sub_act(double energy);
	
}
