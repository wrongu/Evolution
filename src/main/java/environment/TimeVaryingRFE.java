package environment;

import java.util.Random;

import applet.Config;
import bio.organisms.AbstractOrganism;
import environment.generators.IGenerator;


public class TimeVaryingRFE extends RandomFoodEnvironment {

	private Random seed_generator;
	private IGenerator next_generator;
	private long tick_period = Config.instance.getLong("FADE_PERIOD");
	
	public TimeVaryingRFE(double energy_per_unit_food, long seed) {
		super(energy_per_unit_food, seed);
		seed_generator = new Random(seed); // used exclusively for generating next perlin seed
		next_generator = createGenerator(seed_generator.nextLong());
	}
	
	@Override
	public void update(){
		super.update();
		
		if(tickNumber % tick_period == 0){
			swap_generators();
		}
	}

	private void swap_generators(){
		this.generator = next_generator;
		this.next_generator = createGenerator(seed_generator.nextLong());
		System.out.println("swapped");
	}
	
	public double getFade(){
		return (tickNumber % tick_period) / (double) tick_period;
	}
	
	@Override
	protected void feed(AbstractOrganism o){
		double food1 = this.generator.terrainValue(o.getX(), o.getY())*food_energy;
		double food2 = this.next_generator.terrainValue(o.getX(), o.getY())*food_energy;
		double fade = getFade();
		double food = food1 * (1.0-fade) + food2 * fade;
		int numberNearby = grid.getInDisk(o.getX(), o.getY(), food_radius).size();
		o.feed(food/numberNearby);
	}

	public IGenerator getNextGenerator() {
		return next_generator;
	}
}
