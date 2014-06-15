package bio.organisms;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import applet.Config;
import bio.genetics.Gene;
import bio.genetics.IGeneCarrier;
import bio.organisms.brain.BrainFactory;
import bio.organisms.brain.IBrain;
import bio.organisms.brain.IOutput;
import bio.organisms.brain.ISense;
import environment.Environment;

public abstract class AbstractOrganism extends Entity implements IGeneCarrier<AbstractOrganism, Object>{
	
	protected Gene<? extends AbstractOrganism> gene;
	protected IBrain brain;
	protected List<ISense> senses;
	protected List<IOutput> outputs;
	protected double energy;
	protected Environment env;
	
	// debug/tune
	private HashMap<String, Double> energy_drains;
	
	public AbstractOrganism(Environment e,
			Gene<? extends AbstractOrganism> gene,
			double init_energy, double x, double y){
		this.energy = init_energy;
		this.gene = gene;
		this.env = e;
		this.senses = this.createSenses();
		this.outputs = this.createOutputs();
		this.brain = BrainFactory.newBrain(Config.instance.getString("BRAIN_TYPE"), senses.size(), outputs.size(), this, e.getRandom());
		energy_drains = new HashMap<String, Double>();
		this.x = x;
		this.y = y;
	}
	
	protected abstract List<ISense> createSenses();
	protected abstract List<IOutput> createOutputs();
	
	public void feed(double food_energy){
		assert(food_energy >= 0.0);
		this.energy += food_energy;
		if(Double.isNaN(food_energy) || Double.isInfinite(food_energy)){
			System.out.println(Double.isNaN(food_energy) ? "FOOD NAN" : "FOOD INF");
		}
	}
	
	public final void thinkAndAct(){
		if(this.brain != null){
			// set inputs
			for(int s = 0; s < this.senses.size(); s++)
				this.brain.setInput(s, this.senses.get(s).doSense(env, this));
			// compute next state
			this.brain.tick();
			// set outputs
			for(int o = 0; o < this.outputs.size(); o++)
				this.outputs.get(o).act(this.brain.getOutput(o));
		}
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

	public double getEnergy() {
		return this.energy;
	}
	
}
