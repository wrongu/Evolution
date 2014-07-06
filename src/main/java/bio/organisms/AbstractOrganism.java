package bio.organisms;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map.Entry;

import applet.Config;
import bio.genetics.Gene;
import bio.genetics.IGeneCarrier;
import bio.organisms.brain.BrainFactory;
import bio.organisms.brain.IBrain;
import environment.Environment;

public abstract class AbstractOrganism extends Entity implements IGeneCarrier<AbstractOrganism, Object>{
	
	private static final double FEEDING_CONSTANT = 1.0/Config.instance.getDouble("FEEDING_CURVATURE");
	private static final double AGING_LATENCY = Config.instance.getDouble("AGING_LATENCY");
	private static final double AGING_SPEED = Config.instance.getDouble("AGING_SPEED");
	
	protected Gene<? extends AbstractOrganism> gene;
	protected IBrain brain;
	protected double energy;
	protected Environment env;
	protected int age;
	
	// debug/tune
	private HashMap<String, Double> energy_drains;
	
	public AbstractOrganism(Environment e,
			Gene<? extends AbstractOrganism> gene,
			double init_energy, double x, double y){
		this.energy = init_energy;
		this.gene = gene;
		this.env = e;
		this.brain = BrainFactory.newBrain(Config.instance.getString("BRAIN_TYPE"), e.sense_systems.size(), e.action_systems.size(), this, e.getRandom());
		energy_drains = new HashMap<String, Double>();
		this.x = x;
		this.y = y;
		this.age = 0;
	}
	
	public void feed(double food_energy){
		assert(food_energy >= 0.0);
		double curveDeriv = FEEDING_CONSTANT/(this.energy/2 + FEEDING_CONSTANT);
		double ageMult = age > AGING_LATENCY ? 1.0/(AGING_SPEED*(age - AGING_LATENCY) + 1.0) : 1.0;
		this.energy += curveDeriv*ageMult*food_energy;
	}
	
	public final void tick(){
		if(this.brain != null){
			// compute next state
			// this is potentially confusing because of the way senses and actions
			// have been refactored to be more like ECS systems, but each organism
			// still owns its own brain. senses and actions are handled globally
			// outside this function.
			this.brain.tick();
		}
		age++;
	}
	
	/**
	 * precompute the next update (e.g. physics calculate forces but don't act them yet.
	 */
	public abstract void preUpdatePhysics();
	
	/**
	 * perform update that has been computed in preUpdate()
	 * @param dt delta time (seconds)
	 */
	public abstract void updatePhysics(double dt);
	
	/**
	 * check for collisions with another organism (and act on them). BOTH organsims should be updated!
	 * @param other the other Organism to check against.
	 */
	public abstract void collide(AbstractOrganism other);

	/**
	 * Draw a graphical representation of this organism to a Graphics2D canvas
	 * @param g the canvas
	 * @param sx x offset from true position
	 * @param sy y offset from true position
	 * @param scx x scale
	 * @param scy y scale
	 */
	public void draw(Graphics2D g, float sx, float sy, float scx, float scy){}
	
	// TESTING
	public void print_energy_stats(){
		double tot = 0.0;
		for(Double energy : energy_drains.values())
			tot += energy;
		System.out.println("Total Energy Used: " + tot);
		if(tot > 0.0){
			for(Entry<String, Double> pair : energy_drains.entrySet())
				System.out.printf("%20s: %f%%\n", pair.getKey(), (100. * pair.getValue() / tot));
		}
	}
	
	/**
	 * Expend energy
	 * @param requested the amount of energy you would like to use
	 * @param requester a unique name for the requester (used to tally totals)
	 * @return how much energy you can use
	 */
	public double useEnergy(double requested, String requester){
		assert(requested >= 0);
		// can't use more energy than I have left
		double available = Math.min(requested, this.energy);
		// take away energy
		// (deliberately allowing for overdraft since that's the only way is_alive() can fail)
		this.energy -= requested;
		// tally total
		if(this.energy_drains.containsKey(requester))
			energy_drains.put(requester, energy_drains.get(requester) + requested);
		else
			energy_drains.put(requester, requested);
		// return how much can be used
		return available;
	}
	
	public boolean is_alive(){
		return this.energy > 0.0;
	}
	
	public abstract void onDeath();

	public double getEnergy() {
		return this.energy;
	}
	
	public double getBrainOutput(int output_id){
		return this.brain.getOutput(output_id);
	}
	
	public void setBrainInput(int sense_id, double value){
		this.brain.setInput(sense_id, value);
	}
}
