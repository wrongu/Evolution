package bio.organisms.brain.actions;

import environment.Environment;
import applet.Config;
import bio.organisms.AbstractOrganism;
import bio.organisms.SimpleCircleOrganism;
import bio.organisms.brain.ActionSystem;

public class Attack extends ActionSystem{
	
	private double range = Config.instance.getDouble("SCO_EFFECT_RANGE");
	private double foodOnKill = Config.instance.getDouble("SCO_ENERGY_ON_DEATH");

	public Attack(Environment e, int id) {
		super(e, id, Config.instance.getDouble("ACT_POWER_PER_ATTACK"), Config.instance.getDouble("SCO_ATTACK_STRENGTH"));
	}

	@Override
	public void performAll(double dt) {
		
		// First clear last round's attack data.
		for(AbstractOrganism org : env.getAll()){
			if(org instanceof SimpleCircleOrganism) {
				SimpleCircleOrganism sco = (SimpleCircleOrganism) org;
				sco.clearAttackers();
			}
		}
		
		// Now do damage and set this round's attack data.
		for(AbstractOrganism org : env.getAll()){
			if(org instanceof SimpleCircleOrganism) {
				SimpleCircleOrganism sco = (SimpleCircleOrganism) org;
				double brainOutput = sco.getBrainOutput(output_id);
				sco.setAttackOutput(brainOutput);
				double attackPower = POWER_PER_BRAIN_OUTPUT*brainOutput;
				org.useEnergy(attackPower*dt, "Attack");
				double attackStrength = STRENGTH_PER_BRAIN_OUTPUT*brainOutput;
				
				// Adds attackers to prey.
				for(AbstractOrganism o : env.getInDisk(sco.getX(), sco.getY(), range)) {
					if(o instanceof SimpleCircleOrganism && o != sco) {
						SimpleCircleOrganism prey = (SimpleCircleOrganism) o;
						prey.addAttacker(sco, attackStrength*dt);
					}
				}
			}
		}
		
	}

}
