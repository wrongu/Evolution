package environment;

import bio.organisms.AbstractOrganism;
import bio.organisms.SimpleCircleOrganism;
import environment.generators.IGenerator;
import environment.generators.PerlinGenerator;

/**
 * RandomFoodEnvironment will provide food to organisms based solely on a randomly generated terrain.
 * 
 * @author wrongu
 *
 */
public class RandomFoodEnvironment extends Environment {

	private IGenerator generator;
	private double food_energy;

	private double food_radius = 2*SimpleCircleOrganism.DEFAULT_RANGE;
	
	public RandomFoodEnvironment(double energy_per_unit_food, long seed){
		super(seed);
		this.food_energy = energy_per_unit_food;
		this.generator = new PerlinGenerator(4, 20., this.getRandom().nextLong());
		for(int i = 0; i < 100; i++) {
			grid.add(new SimpleCircleOrganism(this, 1.0, (getRandom().nextDouble() - 0.5)*500, (getRandom().nextDouble() - 0.5)*500));
		}
	}
	
	public IGenerator getGenerator(){
		return generator;
	}
	
	@Override
	public void update(){
		super.update();

		for(AbstractOrganism o : grid) {
			double food = this.generator.terrainValue(o.getX(), o.getY())*food_energy;
			int numberNearby = grid.getInDisk(o.getX(), o.getY(), food_radius).size();
			o.feed(food/numberNearby);
		}
	}
}
