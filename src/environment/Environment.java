package environment;

import graphics.IDrawable;
import graphics.opengl.IDrawableGL;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import bio.genetics.DigraphGene;

import structure.Organism;
import structure.OrganismFactory;
import sun.awt.SunToolkit.InfiniteLoop;

public class Environment implements IDrawable, IDrawableGL {

	public static enum Topology {INFINITE, TORUS, SPHERE};

	public List<Organism> organisms;

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
		organisms = new LinkedList<Organism>();
		width = w;
		height = h;
		seedRand = new Random(seed);		
	}

	public Random getRandom(){
		return seedRand;
	}

	public void update(double dt){
		for(Organism o : organisms){
			o.physicsUpdate();
		}

		// TODO faster than o^2 collision checks
		for(int i = 0; i < organisms.size(); i++) {
			for(int j = i+1; j < organisms.size(); j++) {
				organisms.get(i).doCollisions(organisms.get(j));
			}
		}

		for(Organism o : organisms) {
			o.move(dt > 1.0 ? 1.0 : dt);
		}
	}

	public void draw(Graphics2D g, int sx, int sy, double scx, double scy) {
		for(Organism o : organisms)
			o.draw(g, sx, sy, scx, scy);
	}

	public void glDraw() {
		// TODO - draw some sort of background?
		for(Organism o : organisms)
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
