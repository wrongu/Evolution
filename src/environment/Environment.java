package environment;

import graphics.IDrawable;
import graphics.IDrawableGL;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import structure.Organism;
import structure.OrganismFactory;

public class Environment implements IDrawable, IDrawableGL, Runnable {
	
	public static final long TICK_MS = 50;
	public static final double GRAVITY = 0.1;
	
	public double viscosity;
	public double friction;
	public List<Organism> organisms;
	private boolean cont;
	private long last_update;
	private int width, height;
	
	private boolean mousedown = false;
	private int mousex, mousey;
	
	public Environment(int w, int h){
		viscosity = 0.002;
		friction = 0.0;
		organisms = new LinkedList<Organism>();
		// DEBUGGING
		organisms.add(OrganismFactory.testDummy(OrganismFactory.JOINTLESS_SNAKE,this));
		last_update = System.currentTimeMillis();
		width = w;
		height = h;
	}
	
	public void update(){
		long uptime = System.currentTimeMillis();
		double partial_tick = ((double) (uptime - last_update)) / (double) TICK_MS;
		for(Organism o : organisms){
			//o.drift(0, GRAVITY);
			o.physicsUpdate(partial_tick);
			o.contain(this);
			if(mousedown) {
				double dist = Math.sqrt((mousex - o.getX())*(mousex - o.getX()) + (mousey - o.getY())*(mousey - o.getY()));
				o.drift((mousex - o.getX()) / dist, (mousey - o.getY())/ dist);
				System.out.println("Mouse down on: x = " + mousex + ", y = " + mousey + ".");
			}
		}
		mousedown = false;
		last_update = uptime;
	}

	public void draw(Graphics2D g, int sx, int sy, double scx, double scy) {
		for(Organism o : organisms)
			o.draw(g, sx, sy, scx, scy);
	}
	
	public void draw() {
		// TODO - draw some sort of background?
		for(Organism o : organisms)
			o.draw();
	}

	public void run() {
		cont = true;
		while(cont){
			this.update();
		}
	}
	
	public void interrupt(){
		cont = false;
	}
	
	/**
	 * get boundaries of this environment
	 * @return double array [xmin, ymin, xmax, ymax] of environment's bounding area
	 */
	public double[] getBounds(){
		return new double[] {0, 0, width, height};
	}
	
	public void mouseDown(int mx, int my){
		mousedown = true;
		mousex = mx;
		mousey = my;
	}

}
