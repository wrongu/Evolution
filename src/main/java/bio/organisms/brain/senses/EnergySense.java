package bio.organisms.brain.senses;

import environment.Environment;
import bio.organisms.AbstractOrganism;
import bio.organisms.brain.SenseSystem;

public class EnergySense extends SenseSystem {

	public EnergySense(Environment e, int id) {
		super(e, id);
	}

	@Override
	public void senseAll() {
		for (AbstractOrganism orgo : env.getAll()) {
			orgo.setBrainInput(sense_id, orgo.getEnergy());
		}
	}

}
