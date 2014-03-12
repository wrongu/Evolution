package bio.organisms;

import java.awt.Graphics2D;

import environment.Environment;

import bio.genetics.Gene;
import bio.organisms.brain.Brain;

public abstract class AbstractOrganism {

	protected Gene<? extends AbstractOrganism> gene;
	protected Brain brain;
	protected double energy;
	protected double pos_x;
	protected double pos_y;
	protected Environment env;
	
	public AbstractOrganism(Environment e, Gene<? extends AbstractOrganism> gene, double init_energy, double x, double y){
		this.energy = init_energy;
		this.pos_x = x;
		this.pos_y = y;
		this.gene = gene;
		this.env = e;
	}
	
	public void feed(double food_energy){
		assert(food_energy >= 0.0);
		this.energy += food_energy;
	}
	
	/**
	 * 
	 */
	public final void thinkAndAct(){
		this.brain.tick();
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
	public void draw(Graphics2D g, int sx, int sy, double scx, double scy){}
	
	/**
	 * Draw this organism using OpenGL calls. note that scaling and offsets are managed elsewhere
	 */
	public void glDraw(){}
	
	/**
	 * Expend energy
	 * @param requested the amount of energy you would like to use
	 * @return how much energy you can use
	 */
	public double useEnergy(double requested){
		// can't use more energy than I have left
		double available = Math.min(requested, this.energy);
		// take away energy
		// (deliberately allowing for overdraft since that's the only way is_alive() can fail)
		this.energy -= requested;
		// return how much can be used
		return available;
	}
	
	public boolean is_alive(){
		return this.energy > 0.0;
	}
	
	public double getX(){
		return this.pos_x;
	}
	
	public double getY(){
		return this.pos_y;
	}	
}
