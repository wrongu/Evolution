package bio.organisms.brain.senses;

import environment.Environment;
import applet.Config;
import bio.organisms.AbstractOrganism;
import bio.organisms.SimpleCircleOrganism;
import bio.organisms.brain.SenseSystem;

public class Pain extends SenseSystem{
	
	private static final double PAIN_SENSITIVITY = Config.instance.getDouble("SCO_PAIN_SENSITIVITY");

	public Pain(Environment e, int id) {
		super(e, id);
	}

	@Override
	public void senseAll() {
		for(AbstractOrganism org : env.getAll()) {
			if(org instanceof SimpleCircleOrganism) {
				SimpleCircleOrganism sco = (SimpleCircleOrganism) org;
				sco.setBrainInput(sense_id, sco.getDamageThisTurn()*PAIN_SENSITIVITY);
			}
		}
	}

}
