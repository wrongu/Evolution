package structure;

import environment.Environment;
import graphics.IDrawable;
import graphics.RenderPanel;
import graphics.opengl.IDrawableGL;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import bio.ann.ISense;

import physics.Joint;
import physics.PointMass;
import physics.Rod;

public class Organism implements IDrawable, IDrawableGL {
	
	private Brain brain;
	private List<PointMass> pointmasses;
	private List<Rod> rods;
	private List<Joint> joints;
	private List<Muscle> muscles;
	private List<ISense> senses;
	private Environment theEnvironment;
	
	private double energy;
	
	private double x;
	private double y;
	private double radius;
	
	public Organism(Environment e){
		energy = 20.0;
		rods = new LinkedList<Rod>();
		joints = new LinkedList<Joint>();
		pointmasses = new LinkedList<PointMass>();
		muscles = new LinkedList<Muscle>();
		senses = new LinkedList<ISense>();
		// brain = new Brain(senses, muscles);
		theEnvironment = e;
	}
	
	public void initStructure(){
		brain = new Brain(senses, muscles);
		for(int i=0; i<5; i++) {
			physicsUpdate();
			for( PointMass pm : pointmasses ) {
				pm.move(theEnvironment,1.0);
			}
		}
		for(PointMass pm : pointmasses) pm.clearPhysics();
	}
	
	public void physicsUpdate(){
		//brain.update();
		// distribute energy between muscles
		for(Muscle m : muscles)
			// TODO this is backwards. muscles should requestEnergy() _before_ the simulation resolves forces
			energy -= m.act();
		for(Joint j : joints)
			j.physicsUpdate(theEnvironment);
		for(Rod r : rods)
			r.physicsUpdate(theEnvironment);
	}
	
	public void move(double dt) {
		// move point-mass-joints, update center-x and center-y coordinates, and average velocity.
		for(PointMass j : pointmasses){
			j.move(theEnvironment, dt);
		}
		
		updateXYRad();
	}
	
	public void drift(double fx, double fy){
		for(PointMass pm : pointmasses)
			pm.addAcc(fx, fy);
	}

	public void draw(Graphics2D g, int sx, int sy, double scx, double scy) {
		// TODO - draw brain with size according to brain.estimateSize()?/
		g.setColor(RenderPanel.ORGANISM_COLOR);
		for(Rod r : rods)
			r.draw(g, sx, sy, scx, scy);
	}

	public void glDraw() {
		for(Rod r : rods) r.glDraw();
		for(PointMass p : pointmasses) p.glDraw();
	}

	public double requestEnergy(double d) {
		double e = Math.min(energy, d);
		energy -= e;
		return e;
	}
	
	public void addAllPointMasses(List<PointMass> add){
		for(PointMass pm : add) pointmasses.add(pm);
	}
	
	public void addAllRods(List<Rod> add){
		for(Rod r : add) rods.add(r);
	}
	
	public void addAllJoints(List<Joint> add){
		for(Joint j : add) joints.add(j);
	}
	
	public void addAllMuscles(List<Muscle> add){
		for(Muscle m : add) muscles.add(m);
	}

	public void contain(Environment environment) {
		double[] bounds = environment.getBounds();
		for(PointMass pm : pointmasses){
			if(pm.getX() < bounds[0])
				pm.addForce(2*Environment.GRAVITY, 0);
			if(pm.getX() > bounds[2])
				pm.addForce(-2*Environment.GRAVITY, 0);
			if(pm.getY() < bounds[1])
				pm.addForce(0, 2*Environment.GRAVITY);
			if(pm.getY() > bounds[3])
				pm.addForce(0, -2*Environment.GRAVITY);
		}
	}

	public List<PointMass> getPoints() {
		return pointmasses;
	}
	
	// This method for DEBUGGING PURPOSES ONLY!
	public Muscle getFirstMuscle() {
		if(muscles.size() > 0) return muscles.get(0);
		else return null;
	}
	public double getX(){
		return this.pointmasses.get(0).getX();
	}
	public double getY(){
		return this.pointmasses.get(0).getY();
	}

	public void doCollisions(Organism o) {

		// Detect whether or not the organisms are too far apart to collide. Return if yes.
		double dx = x - o.x;
		double dy = y - o.y;
		double mindist = radius + o.radius;
		
		// d619b346d5f85b6c3c5a71623e9b39d4491f8d86
		// ...wat?
		
		if(Math.hypot(dx, dy) >= mindist)
			return;
		
		// Do pointmass on pointmass collisions
		for(PointMass pm1 : pointmasses) {
			for(PointMass pm2 : o.pointmasses) {
				pm1.collide(pm2);
			}
		}
		
		// Do pointmass on rod collisions and rod on pointmass collisions
		for(PointMass pm : pointmasses) {
			for(Rod r : o.rods) {
				pm.collide(r);
			}
		}
		
		for(PointMass pm : o.pointmasses) {
			for(Rod r : rods) {
				pm.collide(r);
			}
		}
	}
	
	private void updateXYRad() {
		x = 0;
		y = 0;
		radius = 0;
		for(PointMass pm : pointmasses) {
			x += pm.getX();
			y += pm.getY();
		}
		x /= (double)pointmasses.size();
		y /= (double)pointmasses.size();
		
		double dx;
		double dy;
		double tempRadius = 0;
		for(PointMass pm : pointmasses) {
			dx = x - pm.getX();
			dy = y - pm.getY();
			tempRadius = Math.sqrt(dx*dx + dy*dy) + pm.getRadius();
			if(tempRadius > radius) {
				radius = tempRadius;
			}
		}
		radius += 10;
	}
	
}
