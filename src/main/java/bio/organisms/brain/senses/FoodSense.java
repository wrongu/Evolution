package bio.organisms.brain.senses;

import environment.Environment;
import environment.RandomFoodEnvironment;
import applet.Config;
import bio.organisms.AbstractOrganism;
import bio.organisms.SimpleCircleOrganism;
import bio.organisms.brain.SenseSystem;

public class FoodSense extends SenseSystem{
	
	private static final double FOOD_SENSITIVITY = Config.instance.getDouble("SCO_FOOD_SENSITIVITY");
	private double offset = 0;

	public FoodSense(Environment e, int id, double offset) {
		super(e, id);
		this.offset = offset;
	}

	@Override
	public void senseAll() {
		for(AbstractOrganism org : env.getAll()) {
			if(org instanceof SimpleCircleOrganism) {
				SimpleCircleOrganism sco = (SimpleCircleOrganism) org;
				double x = sco.getX();
				double y = sco.getY();
				double dirx = sco.getDirX();
				double diry = sco.getDirY();
				double terrain = ((RandomFoodEnvironment)env).getGenerator().terrainValue(x - offset*diry, y + offset*dirx);
				sco.setBrainInput(sense_id, terrain*FOOD_SENSITIVITY);
			}
		}
	}
	

}
