package structure;

import environment.Environment;
import graphics.IDrawable;
import graphics.RenderPanel;
import graphics.opengl.IDrawableGL;

import static org.lwjgl.opengl.ARBBufferObject.*;
import static org.lwjgl.opengl.ARBVertexBufferObject.*;
import static org.lwjgl.opengl.GL11.*;

import java.awt.Graphics2D;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.BufferUtils;

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
		for(i=0; i<5; i++) physicsUpdate(1.0);
		for(PointMass pm : pointmasses) pm.clearPhysics();
		
	}
	
	public void physicsUpdate(double dt){
		brain.update();
		// distribute energy between muscles
		for(Muscle m : muscles)
			energy -= m.act();
		for(Joint j : joints)
			j.physicsUpdate(theEnvironment);
		for(Rod r : rods)
			r.physicsUpdate(theEnvironment);
		// move point-mass-joints, update center-x and center-y coordinates
		double sx = 0.0, sy = 0.0;
		for(PointMass j : pointmasses){
			j.move(theEnvironment, dt);
			sx += j.getX();
			sy += j.getY();
		}
		x = sx / pointmasses.size();
		y = sy / pointmasses.size();
	}
	
	public void drift(double fx, double fy){
		for(PointMass pm : pointmasses)
			pm.addAcc(fx, fy);
	}

	public void draw(Graphics2D g, int sx, int sy, double scx, double scy) {
		g.setColor(RenderPanel.ORGANISM_COLOR);
		for(Rod r : rods)
			r.draw(g, sx, sy, scx, scy);
	}

	public void glDraw() {
		for(Rod r : rods)
			r.glDraw();
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

	public List<PointMass> getPoints() {
		return pointmasses;
	}
	
	// This method for DEBUGGING PURPOSES ONLY!
	public Muscle getFirstMuscle() { return muscles.get(0); }
}
