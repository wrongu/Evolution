package bio.organisms;

import environment.Environment;
import environment.physics.Joint;
import environment.physics.PointMass;
import environment.physics.Rod;
import graphics.IDrawable;
import graphics.RenderPanel;
import graphics.opengl.IDrawableGL;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import bio.genetics.Gene;
import bio.organisms.brain.Brain;
import bio.organisms.brain.ISense;


public class PointRodOrganism extends AbstractOrganism implements IDrawable, IDrawableGL {
	
	private List<PointMass> pointmasses;
	private List<Rod> rods;
	private List<Joint> joints;
	private List<Muscle> muscles;
	private List<ISense> senses;

	private double radius;
	
	public PointRodOrganism(Environment e, Gene<? extends AbstractOrganism> gene){
		super(e, gene, 20.0, 0.0, 0.0);
		rods = new LinkedList<Rod>();
		joints = new LinkedList<Joint>();
		pointmasses = new LinkedList<PointMass>();
		muscles = new LinkedList<Muscle>();
		senses = new LinkedList<ISense>();
	}
	
	@Override
	public void feed(double food_energy){
		this.energy += food_energy;
	}
	
	public void initStructure(){
		brain = new Brain(senses, muscles);
		for(int i=0; i<5; i++) {
			preUpdatePhysics();
			updatePhysics(0.1);
			for( PointMass pm : pointmasses ) {
				pm.move(env,1.0);
			}
		}
		for(PointMass pm : pointmasses) pm.clearPhysics();
	}
	
	@Override
	public void preUpdatePhysics(){
		// muscles act
		for(int i=0; i < muscles.size(); i++){
			if(brain != null)
				muscles.get(i).act(brain.getOutput(i));
		}
		for(Joint j : joints) j.physicsUpdate(env);
		for(Rod r : rods) r.physicsUpdate(env);
	}
	
	@Override
	public void updatePhysics(double dt){
		// move point-mass-joints, update center-x and center-y coordinates, and average velocity.
		for(PointMass p : pointmasses)
			p.move(env, dt);
		updateXYRad();
	}
	
	public void drift(double fx, double fy){
		for(PointMass pm : pointmasses)
			pm.addAcc(fx, fy);
	}
	
	@Override
	public void draw(Graphics2D g, int sx, int sy, double scx, double scy) {
		// TODO - draw brain with size according to brain.estimateSize()?/
		g.setColor(RenderPanel.ORGANISM_COLOR);
		for(Rod r : rods)
			r.draw(g, sx, sy, scx, scy);
	}

	@Override
	public void glDraw() {
		for(Rod r : rods) r.glDraw();
		for(PointMass p : pointmasses) p.glDraw();
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

	public void collide(AbstractOrganism other) {
		if(!(other instanceof PointRodOrganism)) return;
		PointRodOrganism o = (PointRodOrganism) other;

		// Detect whether or not the organisms are too far apart to collide. Return if yes.
		double dx = pos_x - o.pos_x;
		double dy = pos_y - o.pos_y;
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
		double x = 0;
		double y = 0;
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
