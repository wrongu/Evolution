package structure;

import environment.Environment;
import graphics.IDrawable;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

public class Organism implements IDrawable {
	
	private Brain brain;
	private List<Joint> joints;
	private List<Rod> rods;
	private List<Muscle> muscles;
	private List<Sense> senses;
	private Environment theEnvironment;
	
	private double energy;
	private double x, y;
	
	public Organism(double comx, double comy, Environment e){
		energy = 20.0;
		rods = new LinkedList<Rod>();
		joints = new LinkedList<Joint>();
		muscles = new LinkedList<Muscle>();
		senses = new LinkedList<Sense>();
		brain = new Brain(senses, muscles);
		x = comx;
		y = comy;
		theEnvironment = e;
	}
	
	public void initStructure(){
		double sumlen = 0.0;
		for(Rod r : rods)
			sumlen += r.getValue();
		double meanlen = sumlen / rods.size();
		double angle_delta = 2 * Math.PI / joints.size();
		int i = 0;
		for(Joint j : joints){
			j.initPosition(x + Math.cos(i*angle_delta)*meanlen, y + Math.sin(i*angle_delta)*meanlen);
			i++;
		}
	}
	
	public void update(){
		brain.update();
		// distribute energy between muscles
		for(Muscle m : muscles)
			m.act(this);
		for(Joint j : joints)
			j.forceConnectingStructures();
		for(Rod r : rods)
			r.forceConnectingStructures();
		// move point-mass-joints, update center-x and center-y coordinates
		double sx = 0.0, sy = 0.0;
		for(Joint j : joints){
			j.move(theEnvironment);
			sx += j.getX();
			sy += j.getY();
		}
		x = sx / joints.size();
		y = sy / joints.size();
	}

	public void draw(Graphics2D g) {
		// TODO - draw brain with size according to brain.estimateSize()?/
		for(Rod r : rods)
			r.draw(g);
		// TODO - add glow to represent energy?
	}

	public double requestEnergy(double d) {
		double e = Math.min(energy, d);
		energy -= e;
		return e;
	}
}
