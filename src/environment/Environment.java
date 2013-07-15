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
	
	public double viscosity;
	public double friction;
	public List<Organism> organisms;
	private int width, height;
	
	private int[] mouse_in;
	private boolean[] mouse_down;
	private boolean mousedown = false;
	private int mousex, mousey;
	
	public Environment(int w, int h){
		viscosity = 0.002;
		friction = 0.0;
		organisms = new LinkedList<Organism>();
		// DEBUGGING
		organisms.add(OrganismFactory.testDummy(OrganismFactory.TRIANGLE_WITH_MUSCLE,this));
		width = w;
		height = h;
	}
	
	public void update(double dt){
		mousedown = mouse_down[0];
		mousex = mouse_in[0];
		mousey = mouse_in[1];
	
		dt /= 100.0;
		for(Organism o : organisms){
			o.drift(0, -GRAVITY);
			o.physicsUpdate(dt > 1.0 ? 1.0 : dt);
			o.contain(this);
			
			 if(mousedown) {
			 	double dist = Math.sqrt((mousex - o.getX())*(mousex - o.getX()) + (mousey - o.getY())*(mousey - o.getY()));
			 	o.drift((mousex - o.getX()) / dist, (mousey - o.getY())/ dist);
			 	System.out.println("Mouse down on: x = " + mousex + ", y = " + mousey + ".");
			 }
		}

		 mousedown = false;
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
		mousedown = true;
		mousex = mx;
		mousey = my;
	}

	public void bindInput(boolean[] mouse_down, int[] mouse_move) {
		this.mouse_in = mouse_move;
		this.mouse_down = mouse_down;
	}

}
