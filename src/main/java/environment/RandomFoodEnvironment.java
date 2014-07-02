package environment;

import java.util.Arrays;

import applet.Config;
import bio.organisms.AbstractOrganism;
import bio.organisms.SimpleCircleOrganism;
import bio.organisms.brain.actions.Accelerate;
import bio.organisms.brain.actions.Mitosis;
import bio.organisms.brain.actions.Turn;
import bio.organisms.brain.senses.EnergySense;
import environment.generators.IGenerator;
import environment.generators.PerlinGenerator;

/**
 * RandomFoodEnvironment will provide food to organisms based solely on a randomly generated terrain.
 * 
 * @author wrongu
 *
 */
public class RandomFoodEnvironment extends Environment {

	protected IGenerator generator;
	protected double food_energy;
	private static final double SPAWN_RATE = 0.01;
	private static final double SPAWN_RADIUS = 50;
	private static final double TAPER = Config.instance.getDouble("PERLIN_TAPER");

	protected double food_radius = 2*SimpleCircleOrganism.DEFAULT_RANGE;
	
	public RandomFoodEnvironment(double energy_per_unit_food, long seed){
		super(seed);
		this.food_energy = energy_per_unit_food;
		this.generator = createGenerator(seed);
	}
	
	protected IGenerator createGenerator(long seed){
		int perlin_octaves = Config.instance.getInt("PERLIN_OCTAVES");
		double perlin_scale = Config.instance.getDouble("PERLIN_SCALE");
		IGenerator gen = new PerlinGenerator(perlin_octaves, perlin_scale, seed, new PerlinGenerator.Filter() {
			
			private double TAU = getTau();
			
			@Override
			public double applyFilter(double val, double x, double y) {
				double r2 = x * x + y * y;
				return val * Math.exp(-r2/TAU);
			}
		});
		return gen;
	}
	
	public IGenerator getGenerator(){
		return generator;
	}
	
	public double getTau(){
		return -TAPER*TAPER / Math.log(0.5);
	}
	
	@Override
	public void update(){
		super.update();

		for(AbstractOrganism o : grid) {
			feed(o);
		}
		
		// Spawn random organisms
		if(getRandom().nextDouble() < SPAWN_RATE) {
			spawnRandomOrganism();
		}
	}
	
	protected void feed(AbstractOrganism o){
		double food = this.generator.terrainValue(o.getX(), o.getY())*food_energy;
		int numberNearby = grid.getInDisk(o.getX(), o.getY(), food_radius).size();
		o.feed(food/numberNearby);
	}
	
	public void spawnRandomOrganism() {
		if(grid.getCount() == 0) {
			next_organisms.add(new SimpleCircleOrganism(this, Config.instance.getDouble("INIT_ENERGY"), (getRandom().nextDouble() - 0.5)*SPAWN_RADIUS, (getRandom().nextDouble() - 0.5)*SPAWN_RADIUS));
		} else {
			int n = this.getRandom().nextInt(grid.getCount());
			for(AbstractOrganism o : grid) {
				if(n-- == 0) {
					next_organisms.add(new SimpleCircleOrganism(this, 1.0, o.getX() + (getRandom().nextDouble() - 0.5)*SPAWN_RADIUS, o.getY() + (getRandom().nextDouble() - 0.5)*SPAWN_RADIUS));
					break;
				}
			}
		}
		
	}

	@Override
	protected void initSensesAndActions() {
		// make action and sense systems
		sense_systems = Arrays.asList(
				new EnergySense(this,0));
		action_systems = Arrays.asList(
				new Accelerate(this, 0),
				new Turn(this, 1, Turn.Direction.LEFT),
				new Turn(this, 2, Turn.Direction.RIGHT),
				new Mitosis(this, 3));
	}
}
