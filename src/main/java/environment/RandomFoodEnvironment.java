package environment;

import utils.grid.Chunk;
import bio.organisms.AbstractOrganism;
import environment.generators.IGenerator;
import environment.generators.PerlinGenerator;
import environment.physics.PointMass;

/**
 * RandomFoodEnvironment will provide food to organisms based solely on a randomly generated terrain.
 * 
 * @author wrongu
 *
 */
public class RandomFoodEnvironment extends Environment {
	
	private IGenerator generator;
	private double food_energy;
	
	public RandomFoodEnvironment(double energy_per_unit_food, long seed){
		super(seed);
		this.food_energy = energy_per_unit_food;
		this.generator = new PerlinGenerator(4, 20., this.getRandom().nextLong());
	}
	
	@Override
	public void update(double dt){
		super.update(dt);
		
		for(Chunk c : this.grid) {
			for(AbstractOrganism o : c){
				double base_value = this.generator.terrainValue(o.getX(), o.getY());
				// TODO - is food continuous or is it randomly all-or-nothing?
				double food = this.seedRand.nextDouble() < base_value ? food_energy : 0.0;
				o.feed(food);
			}
		}
	}

	@Override
	protected void doCollisions() {
		for(Chunk c : this.grid){
			for(AbstractOrganism a : c){
				for(AbstractOrganism b : this.getNearby(a, 2*PointMass.DEFAULT_RADIUS, true)){
					a.collide(b);
				}
			}
		}
	}
	
}
