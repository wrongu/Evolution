package bio.organisms.brain.actions;

import environment.Environment;
import applet.Config;
import bio.organisms.AbstractOrganism;
import bio.organisms.SimpleCircleOrganism;
import bio.organisms.brain.ActionSystem;

public class Turn extends ActionSystem {
	
	public static enum Direction {LEFT, RIGHT};
	
	private int dirMult = 0;

	public Turn(Environment e, int id, Direction dir) {
		super(	e,
				id,
				Config.instance.getDouble("ACT_POWER_PER_TURN"),
				Config.instance.getDouble("ACT_STRENGTH_PER_TURN"));
		
		dirMult = (dir == Direction.LEFT ? 1 : (dir == Direction.RIGHT ? -1 : 0));
	}

	@Override
	public void performAll(double dt) {
		for (AbstractOrganism orgo : env.getAll()) {
			if(orgo instanceof SimpleCircleOrganism){
				SimpleCircleOrganism sco = (SimpleCircleOrganism) orgo;
				double neuro = sco.getBrainOutput(output_id);
				double energy = dt * POWER_PER_BRAIN_OUTPUT * neuro * sco.getAgingMultiplier();
				sco.useEnergy(energy, "Turn");
				double strength = dt * STRENGTH_PER_BRAIN_OUTPUT * neuro;
//				double speed = sco.getSpeed();
				sco.addTurn(dirMult*strength);
//				if(speed > 0.0){
//					// (x,y) => (-y,x) to add a normal force
//					double uy = - sco.getVX() / speed;
//					double ux =   sco.getVY() / speed;
//					sco.addExternalForce(ux * strength, uy * strength);
//				}
			}
		}
	}
}
