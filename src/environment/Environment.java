package environment;

import graphics.IDrawable;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import structure.Organism;
import structure.OrganismFactory;

public class Environment implements IDrawable, Runnable {
	public double viscosity;
	public List<Organism> organisms;
	private boolean cont;
	
	private static OrganismFactory tester;
	
	public Environment(){
		viscosity = 0.1;
		organisms = new LinkedList<Organism>();
		// DEBUGGING
	}
	
	public void update(){
		for(Organism o : organisms)
			o.update();
	}

	public void draw(Graphics2D g) {
		for(Organism o : organisms)
			o.draw(g);
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
		return new double[] {0, 0, 800, 600};
	}
	
	static{
		tester = new OrganismFactory();
		// TODO - build it?
	}
}
