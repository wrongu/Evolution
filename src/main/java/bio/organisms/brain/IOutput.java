package bio.organisms.brain;

import bio.organisms.AbstractOrganism;

public abstract class IOutput {
	
	protected AbstractOrganism theOrganism;
	private double multiplier;
	private String name;
	
	public IOutput(AbstractOrganism o, double strength, String n){
		this.theOrganism = o;
		this.multiplier = strength;
		this.name = n;
	}
	
	/**
	 * A wrapper for subclass Outputs that ensures the organism uses energy
	 * @param energy requested energy
	 * @param o the parent organism
	 */
	public final void act(double energy){
		double request = Math.abs(energy) * this.multiplier;
		double e = theOrganism.useEnergy(request, name);
		this.sub_act(e);
	}
	
	protected abstract void sub_act(double energy);
	
}
