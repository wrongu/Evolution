package environment;

import graphics.IDrawable;
import graphics.opengl.IDrawableGL;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import bio.organisms.AbstractOrganism;

public class Environment implements IDrawable, IDrawableGL {

	public static enum Topology {INFINITE, TORUS, SPHERE};
	public static final double TIME_STEP = 0.1;

	protected List<AbstractOrganism> organisms;
	private List<AbstractOrganism> next_organisms;

	protected Topology topology;
	protected double width, height;
	protected Random seedRand;

	// TODO factor out physics separately
	public static double FRICTION = 0.1;
	public static double VISCOSITY = 0.004;

	public Environment(long seed){
		this(0D, 0D, Topology.INFINITE, seed);
	}

	public Environment(double w, double h, Topology t, long seed){
		// LinkedList because we only ever loop over them as a group, and we want fast insertion and removal
		organisms = new LinkedList<AbstractOrganism>();
		next_organisms = new LinkedList<AbstractOrganism>();
		width = w;
		height = h;
		seedRand = new Random(seed);		
	}

	public Random getRandom(){
		return seedRand;
	}
	
	public void addOrganism(AbstractOrganism orgo){
		next_organisms.add(orgo);
	}

	public void update(double dt){
		// TODO remove dt entirely?
		// The environment is no longer pseudo-randomly deterministic if we let dt
		// depend on computer speeds.
		dt = TIME_STEP;
		
		// before going anywhere.. add new babies to the population and check for dead organisms
		for(AbstractOrganism baby : next_organisms)
			organisms.add(baby);
		next_organisms = new LinkedList<AbstractOrganism>();
		for(AbstractOrganism o : organisms)
			if(! o.is_alive()) organisms.remove(o);
		
		// first, process inputs and prepare outputs
		for(AbstractOrganism o : organisms)
			o.thinkAndAct();

		// second (before real physics update), check for collisions
		// TODO faster than O(o^2) collision checks
		for(int i = 0; i < organisms.size(); i++) {
			for(int j = i+1; j < organisms.size(); j++) {
				organisms.get(i).collide(organisms.get(j));
			}
		}
		
		// next, prepare physics updates
		for(AbstractOrganism o : organisms)
			o.preUpdatePhysics();
		
		// finally, update the physics engine
		for(AbstractOrganism o : organisms)
			o.updatePhysics(dt);
	}

	public void draw(Graphics2D g, float sx, float sy, float scx, float scy) {
		for(AbstractOrganism o : organisms)
			o.draw(g, sx, sy, scx, scy);
	}

	public void glDraw() {
		// TODO - draw some sort of background?
		for(AbstractOrganism o : organisms)
			o.glDraw();
	}

	/**
	 * get boundaries of this environment
	 * @return double array [xmin, ymin, xmax, ymax] of environment's bounding area
	 */
	public double[] getBounds(){
		return new double[] {-width/2, -height/2, width/2, height/2};
	}

}
