package environment;

import graphics.IDrawable;
import graphics.opengl.IDrawableGL;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import structure.Organism;
import structure.OrganismFactory;

public class Environment implements IDrawable, IDrawableGL {
	
	public static final double GRAVITY = 0.1;
	
	public double rod_visc;
	public double point_visc;
	public List<Organism> organisms;
	private int width, height;
	
	public Environment(int w, int h){
		rod_visc = 0.01;
		point_visc = 0.01;
		organisms = new LinkedList<Organism>();
		width = w;
		height = h;
		// DEBUGGING
		organisms.add(OrganismFactory.testDummy(this));
	}
	
	public void update(double dt){
		dt /= 100.0;
		for(Organism o : organisms){
			o.drift(0, -GRAVITY);
			o.physicsUpdate(dt > 1.0 ? 1.0 : dt);
			o.contain(this);
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
		return new double[] {0, 0, width, height};
	}
	
	public void mouseDown(int mx, int my){
		for(Organism o : organisms){
			o.drift(-(mx - o.getX() / 10000.0), -(my - o.getY() / 10000.0));
		}
	}

}
