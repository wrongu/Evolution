package environment;

import graphics.IDrawable;
import graphics.IDrawableGL;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import structure.Organism;
import structure.OrganismFactory;

public class Environment implements IDrawable, IDrawableGL {
	
	public static final long TICK_MS = 50;
	public static final double GRAVITY = 0.1;
	
	public double viscosity;
	public double friction;
	public List<Organism> organisms;
	private long last_update;
	private int width, height;
	
	public Environment(int w, int h){
		viscosity = 0.01;
		friction = 0.05;
		organisms = new LinkedList<Organism>();
		// DEBUGGING
		organisms.add(OrganismFactory.testDummy(this));
		last_update = System.currentTimeMillis();
		width = w;
		height = h;
	}
	
	public void update(){
		long uptime = System.currentTimeMillis();
		double partial_tick = ((double) (uptime - last_update)) / (double) TICK_MS;
		for(Organism o : organisms){
			o.drift(0, GRAVITY);
			o.physicsUpdate(partial_tick > 1.0 ? 1.0 : partial_tick);
			o.contain(this);
		}
		last_update = uptime;
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
		return new double[] {0, 0, width, height};
	}
	
	public void mouseDown(int mx, int my){
		for(Organism o : organisms){
			o.drift(-(mx - o.getX() / 10000.0), -(my - o.getY() / 10000.0));
		}
	}

}
