package environment;

import environment.physics.PointMass;
import graphics.IDrawable;
import graphics.opengl.IDrawableGL;

import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import utils.grid.Chunk;
import utils.grid.Grid;
import bio.organisms.AbstractOrganism;

public abstract class Environment implements IDrawable, IDrawableGL {

	public static enum Topology {INFINITE, TORUS, SPHERE};
	public static final double TIME_STEP = 0.1;
	
	private static final int TICKS_PER_EMPTY = 1;

//	protected List<AbstractOrganism> organisms;
	// TODO make grid more abstract
	protected Grid grid;
	private List<AbstractOrganism> next_organisms;

	protected Topology topology;
	protected double width, height;
	protected Random seedRand;
	private long tickNumber;

	// TODO factor out physics separately
	public static double FRICTION = 0.1;
	public static double VISCOSITY = 0.000;

	public Environment(long seed){
		this(0D, 0D, Topology.INFINITE, seed);
	}

	public Environment(double w, double h, Topology t, long seed){
		grid = new Grid();
		next_organisms = new LinkedList<AbstractOrganism>();
		width = w;
		height = h;
		seedRand = new Random(seed);	
		tickNumber = 0;
	}
	
	public Random getRandom(){
		return seedRand;
	}
	
	public void addOrganism(AbstractOrganism orgo){
		next_organisms.add(orgo);
	}

	public void update(double dt){
		System.out.println("Tick #: " + tickNumber + " Organism count: " + grid.getCount());
		// TODO remove dt entirely?
		// The environment is no longer pseudo-randomly deterministic if we let dt
		// depend on computer speeds.
		dt = TIME_STEP;
		
		// if it is time to do so, clean out the Grid.
		if(tickNumber % TICKS_PER_EMPTY == 0) {
			grid.removeEmpties();
		}
		
		// before going anywhere.. add new babies to the population and check for dead organisms
		for(AbstractOrganism baby : next_organisms)
			grid.add(baby);
		next_organisms = new LinkedList<AbstractOrganism>();
		for(Chunk c : grid) {
			for(Iterator<AbstractOrganism> i = c.iterator(); i.hasNext(); ) {
				AbstractOrganism o = i.next();
				if(! o.is_alive()){
					i.remove();
					o.print_energy_stats();
				}
			}
		}
		
		// first, process inputs and prepare outputs
		for(Chunk c : grid) {
			for(AbstractOrganism o : c) {
				o.thinkAndAct();
			}
		}

		// second (before real physics update), check for collisions
		// TODO faster than O(o^2) collision checks
		//doCollisions();
		
		// next, prepare physics updates
		for(Chunk c : grid)
			for(AbstractOrganism o : c)
				o.preUpdatePhysics();
		
		// finally, update the physics engine
		for(Chunk c : grid)
			for(AbstractOrganism o : c)
				o.updatePhysics(dt);
		
		// update grid structure.
		grid.updateChunks();
		
		tickNumber++;
	}
	
	public void draw(Graphics2D g, float sx, float sy, float scx, float scy) {
		for(Chunk c : grid)
			for(AbstractOrganism o : c)
				o.draw(g, sx, sy, scx, scy);
	}

	public void glDraw() {
		// TODO - draw some sort of background?
		for(Chunk c : grid)
			for(AbstractOrganism o : c)
				o.glDraw();
	}

	/**
	 * Returns a HashSet of AbstractOrganisms which are
	 * approximately within a radius r of (x,y), world
	 * coordinates. Further checking is necessary to ensure
	 * that all organisms are actually within r.
	 * 
	 * @param x
	 * @param y
	 * @param r
	 * @return
	 */
	public HashSet<AbstractOrganism> getNearby(double x, double y, double r) {
		
		HashSet<AbstractOrganism> orgos = new HashSet<AbstractOrganism>();
		
		for(Chunk c : grid.getAllWithin(x/Chunk.SIZE, y/Chunk.SIZE, r/Chunk.SIZE)) {
			orgos.addAll(c);
		}
		
		return orgos;
	}
	
	public HashSet<AbstractOrganism> getNearby(AbstractOrganism o, double r, boolean exclude_self) {
		
		HashSet<AbstractOrganism> orgos = new HashSet<AbstractOrganism>();
		double gridX = (o.getX()/Chunk.SIZE);
		double gridY = (o.getY()/Chunk.SIZE);
		double gridR = r/Chunk.SIZE;
		
		for(Chunk c : grid.getAllWithin(gridX, gridY, gridR)) {
			orgos.addAll(c);
		}
		
		if(exclude_self) orgos.remove(o);
		
		return orgos;
	}
	
	/**
	 * To be used with mutual interactions.
	 * 
	 * @param o
	 * @param r
	 * @return
	 */
	public HashSet<AbstractOrganism> getNearbyAsym(AbstractOrganism o, double r) {
		
		HashSet<AbstractOrganism> orgos = new HashSet<AbstractOrganism>();
		int gridX = (int)Math.floor(o.getX()/Chunk.SIZE);
		int gridY = (int)Math.floor(o.getY()/Chunk.SIZE);
		double gridR = r/Chunk.SIZE;
		HashSet<Chunk> nearChunks = grid.getAllWithinAsym(gridX, gridY, gridR);
		Chunk homeChunk = grid.get(gridX, gridY);
		nearChunks.remove(homeChunk);
		
		for(Chunk c : nearChunks) {
			orgos.addAll(c);
		}
		for(AbstractOrganism org : homeChunk) {
			if(org.getY() > o.getY() || (org.getY() == o.getY() && org.getX() > o.getX()) ) {
				orgos.add(org);
			}
		}
		
		return orgos;
	}
	
	public int getOrganismCount(){
		return grid.getCount();
	}
	
	/**
	 * get boundaries of this environment
	 * @return double array (xmin, ymin, xmax, ymax) of environment's bounding area
	 */
	public double[] getBounds(){
		return new double[] {-width/2, -height/2, width/2, height/2};
	}
	
	public long getTickNumber() {
		return tickNumber;
	}
	
	protected void doCollisions(){
		for(Chunk c : this.grid){
			for(AbstractOrganism a : c){
				// TODO remove dependency on PointMass
				for(AbstractOrganism b : this.getNearbyAsym(a, 2*PointMass.DEFAULT_RADIUS)){
					a.collide(b);
				}
			}
		}
	}
	
}
