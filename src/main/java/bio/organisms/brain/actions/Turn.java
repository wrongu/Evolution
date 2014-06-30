package bio.organisms.brain.actions;

import environment.Environment;
import applet.Config;
import bio.organisms.AbstractOrganism;
import bio.organisms.SimpleCircleOrganism;
import bio.organisms.brain.ActionSystem;

public class Turn extends ActionSystem {

	public Turn(Environment e, int id) {
		super(	e,
				id,
				Config.instance.getDouble("ACT_POWER_PER_TURN"),
				Config.instance.getDouble("ACT_STRENGTH_PER_TURN"));
	}

	@Override
	public void performAll(double dt) {
		for (AbstractOrganism orgo : env.getAll()) {
			if(orgo instanceof SimpleCircleOrganism){
				SimpleCircleOrganism sco = (SimpleCircleOrganism) orgo;
				double neuro = sco.getBrainOutput(output_id);
				double energy = dt * POWER_PER_BRAIN_OUTPUT * neuro;
				sco.useEnergy(energy, "Accelerate");
				double strength = dt * STRENGTH_PER_BRAIN_OUTPUT * neuro;
				double speed = sco.getSpeed();
				if(speed > 0.0){
					// (x,y) => (-y,x) to add a normal force
					double uy = - sco.getVX() / speed;
					double ux =   sco.getVY() / speed;
					sco.addExternalForce(ux * strength, uy * strength);
				}
			}
		}
	}
}
