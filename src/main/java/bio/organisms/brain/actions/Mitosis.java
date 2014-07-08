package bio.organisms.brain.actions;

import environment.Environment;
import applet.Config;
import bio.organisms.AbstractOrganism;
import bio.organisms.SimpleCircleOrganism;
import bio.organisms.brain.ActionSystem;

public class Mitosis extends ActionSystem{
	
	private double MITOSIS_THRESHOLD = Config.instance.getDouble("ACT_MITOSIS_THRESHOLD");

	public Mitosis(Environment e, int id) {
		super(e, id, 0, 0);
	}

	@Override
	public void performAll(double dt) {
		for(AbstractOrganism org : env.getAll()) {
			if(org instanceof SimpleCircleOrganism) {
				SimpleCircleOrganism sco = (SimpleCircleOrganism)org;
				
				if(env.getRandom().nextDouble() < sco.getBrainOutput(output_id) && sco.getEnergy() > SimpleCircleOrganism.ENERGY_ON_DEATH) {
					/*
					 * Okay here is where we have options.
					 * 1. Have the parent give birth to offsping - age is not reset.
					 * 2. Have parent birth offspring and reset parent's age.
					 * 3. Delete parent and produce two new offspring.
					 * Right now, we are set at 1, because it's easiest. We will
					 * re-address this when we have a better implementation for
					 * age.
					 */
					SimpleCircleOrganism offspring = (SimpleCircleOrganism) sco.beget(env, null);
					
					// if(offspring.is_alive())
					env.addOrganism(offspring);
				}
			}
		}
	}

}
