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
	private double x, y;
	private double velX, velY;
	private double maxSpeed;
	private double radius;
	
	public Organism(double comx, double comy, Environment e){
		energy = 20.0;
		rods = new LinkedList<Rod>();
		joints = new LinkedList<Joint>();
		pointmasses = new LinkedList<PointMass>();
		muscles = new LinkedList<Muscle>();
		senses = new LinkedList<ISense>();
		// brain = new Brain(senses, muscles);
		x = comx;
		y = comy;
		maxSpeed = 0;
		theEnvironment = e;
	}
	
	public void initStructure(){
		brain = new Brain(senses, muscles);
		double sumlen = 0.0;
		for(Rod r : rods)
			sumlen += 0.5*(r.getRestValue1() + r.getRestValue2());
		double meanhalflen = sumlen / rods.size() / 2;
		double angle_delta = 2 * Math.PI / pointmasses.size();
		int i = 0;
		for(PointMass j : pointmasses){
			j.initPosition(x + Math.cos(i*angle_delta)*meanhalflen, y + Math.sin(i*angle_delta)*meanhalflen);
			i++;
		}
		for(i=0; i<5; i++) {
			physicsUpdate();
			for( PointMass pm : pointmasses ) {
				pm.move(theEnvironment,1.0);
			}
		}
		for(PointMass pm : pointmasses) pm.clearPhysics();
	}
	
	public void physicsUpdate(){
		brain.update();
		// distribute energy between muscles
		for(Muscle m : muscles)
			energy -= m.act();
		for(Joint j : joints)
			j.physicsUpdate(theEnvironment);
		for(Rod r : rods)
			r.physicsUpdate(theEnvironment);
	}
	
	public void move(double dt) {
		// move point-mass-joints, update center-x and center-y coordinates, and average velocity.
		double sx = 0.0, sy = 0.0;
		double svx = 0, svy = 0, sm = 0;
		double m = 0;
		for(PointMass j : pointmasses){
			j.move(theEnvironment, dt);
			m = j.getMass();
			sx += j.getX()*m;
			sy += j.getY()*m;
			svx = j.getVX()*m;
			svy = j.getVY()*m;
			sm += m;
		}
		x = sx / sm;
		y = sy / sm;
		velX = svx / sm;
		velY = svy / sm;

		radius = 0;
		maxSpeed = 0;
		double dx, dy;
		double dvx, dvy;
		for(PointMass p : pointmasses) {
			dx = p.getX() - x;
			dy = p.getY() - y;
			radius = Math.max(radius, (dx)*(dx) + (dy)*(dy));
			dvx = p.getVX() - velX;
			dvy = p.getVY() - velY;
			maxSpeed = Math.max(maxSpeed, dvx*dvx + dvy*dvy);
		}
		radius = Math.sqrt(radius);
		maxSpeed = Math.sqrt(maxSpeed);
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
		// TODO - add glow to represent energy? <-- can only be done in opengl, I think
	}

	public void glDraw() {
		for(Rod r : rods) r.glDraw();
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
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public double getVelX() {
		return velX;
	}
	
	public double getVelY() {
		return velY;
	}
	
	public double getSpeed() {
		return Math.sqrt(velX*velX + velY*velY);
	}

	public List<PointMass> getPoints() {
		return pointmasses;
	}
	
	// This method for DEBUGGING PURPOSES ONLY!
	public Muscle getFirstMuscle() {
		if(muscles.size() > 0) return muscles.get(0);
		else return null;
	}
	public double getRadius() { return radius; }

	public void doCollisions(Organism o) {
		// Detect whether or not the organisms are too far apart to collide. Return if yes.
		double dx = x - o.getX();
		double dy = y - o.getY();
		double mindist = radius + o.radius;
		
		if(dx*dx + dy*dy >= mindist*mindist)
			return;
		
		// Do pointmass on pointmass collisions
		for(PointMass pm1 : pointmasses) {
			for(PointMass pm2 : o.pointmasses) {
				pm1.collide(pm2);
			}
		}
		
		// Do pointmass on rod collisions and rod on pointmass collisions
		
	}
	
}
