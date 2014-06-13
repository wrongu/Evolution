package environment;

import graphics.IDrawable;
import graphics.opengl.IDrawableGL;

import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import utils.grid.Grid;
import applet.Config;
import bio.organisms.AbstractOrganism;
import bio.organisms.Entity;

public abstract class Environment implements IDrawable, IDrawableGL {

	public static enum Topology {INFINITE, TORUS, SPHERE};
	private static final double TIME_STEP = Config.instance.getDouble("DT_TIMESTEP"); // must be held constant for deterministic simulation
	public static final int GRID_SIZE = Config.instance.getInt("CHUNK_SIZE");
	
	private static final int TICKS_PER_EMPTY = Config.instance.getInt("CLEANUP_EVERY");

//	protected List<AbstractOrganism> organisms;
	// TODO make grid more abstract
	protected Grid<AbstractOrganism> grid;
	protected List<AbstractOrganism> next_organisms;

	protected Topology topology;
	protected double width, height;
	protected Random seedRand;
	private long tickNumber;

	public static double FRICTION = Config.instance.getDouble("FRICTION");
	public static double VISCOSITY = Config.instance.getDouble("VISCOSITY");

	public Environment(long seed){
		this(0D, 0D, Topology.INFINITE, seed);
	}

	public Environment(double w, double h, Topology t, long seed){
		grid = new Grid<AbstractOrganism>(GRID_SIZE);
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

	public void update(){
		System.out.println("Tick #: " + tickNumber + " Organism count: " + grid.getCount());

		double dt = TIME_STEP;
		
		// if it is time to do so, clean out the Grid.
		if(tickNumber % TICKS_PER_EMPTY == 0) {
			grid.removeEmpties();
		}
		
		// before going anywhere.. add new babies to the population and check for dead organisms
		for(AbstractOrganism baby : next_organisms)
			grid.add(baby);
		next_organisms = new LinkedList<AbstractOrganism>();
		
		for(Iterator<AbstractOrganism> i = grid.iterator(); i.hasNext();) {
			AbstractOrganism o = i.next();
			if(! o.is_alive()){
				i.remove();
				o.print_energy_stats();
			}
		}
		
		// first, process inputs and prepare outputs
		for(AbstractOrganism o : grid) {
				o.thinkAndAct();
		}

		// second (before real physics update), check for collisions
		// TODO faster than O(o^2) collision checks
		//doCollisions();
		
		// next, prepare physics updates
		for(AbstractOrganism o : grid)
				o.preUpdatePhysics();
		
		// finally, update the physics engine
		for(AbstractOrganism o : grid)
				o.updatePhysics(dt);
		
		// update grid structure.
		grid.updateChunks();
		
		tickNumber++;
	}
	
	public void draw(Graphics2D g, float sx, float sy, float scx, float scy) {
		for(AbstractOrganism o : grid)
				o.draw(g, sx, sy, scx, scy);
	}

	public void glDraw() {
		// TODO - draw some sort of background?
		// TODO - ONLY RENDER THOSE ORGANISMS IN THE VIEWING BOX USING grid.getInBox(...)!
		for(AbstractOrganism o : grid)
			o.glDraw();
	}

	/**
	 * Returns a LinkedList of AbstractOrganisms which are
	 * within a radius r of (x,y). 
	 * 
	 * @param x
	 * @param y
	 * @param r
	 * @return
	 */
	public LinkedList<AbstractOrganism> getInDisk(double x, double y, double r) {
		LinkedList<AbstractOrganism> orgs = new LinkedList<AbstractOrganism>();
		for(Entity o : grid.getInDisk(x,y,r)) {
			orgs.add((AbstractOrganism)o);
		}
		return orgs;
	}
	
	public LinkedList<AbstractOrganism> getInDiskMut(double x, double y, double r) {
		LinkedList<AbstractOrganism> orgs = new LinkedList<AbstractOrganism>();
		for(Entity o : grid.getInDiskMut(x,y,r)) {
			orgs.add((AbstractOrganism)o);
		}
		return orgs;
	}
	
	public LinkedList<AbstractOrganism> getInBox(float ... bounds) {
		if(bounds.length != 4) return null;
		LinkedList<AbstractOrganism> orgs = new LinkedList<AbstractOrganism>();
		for(Entity o : grid.getInBox(bounds[0], bounds[2], bounds[1], bounds[3])) {
			orgs.add((AbstractOrganism)o);
		}
		return orgs;
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
	
//	protected void doCollisions(){
//		for(Chunk c : this.grid){
//			for(AbstractOrganism a : c){
//				// TODO remove dependency on PointMass
//				for(AbstractOrganism b : this.getNearbyAsym(a, 2*PointMass.DEFAULT_RADIUS)){
//					a.collide(b);
//				}
//			}
//		}
//	}
	
}
