package structure;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2d;
import environment.Environment;
import graphics.IDrawable;
import graphics.RenderPanel;
import graphics.opengl.IDrawableGL;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

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
	private DoubleMatrix pos, vel, acc, force, mass, massRec, speed; // PointMass matrix information
	private DoubleMatrix rodValues, rodPoints0, rodPoints1, rodPoints0Tran, rodPoints1Tran, rodMuscles; // Rod matrix information
	private DoubleMatrix jointValues, jointPoints0, jointPoints1, jointPoints0Tran, jointPoints1Tran, jointPoints2Tran, jointPoints2, jointMuscles; // Joint matrix information
	
	private double energy;
	private double x, y;
	private double radius;
	private boolean selfCollisions;
	private boolean otherCollisions;
	
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
		// Initialize matrices.
		int numpoints = pointmasses.size();
		pos = DoubleMatrix.zeros(numpoints,2);
		vel = DoubleMatrix.zeros(numpoints,2);
		speed = Util.mag(vel);
		acc = DoubleMatrix.zeros(numpoints,2);
		force = DoubleMatrix.zeros(numpoints,2);
		mass = DoubleMatrix.zeros(numpoints,1);
		int numrods = rods.size();
		rodValues = DoubleMatrix.zeros(numrods,2);
		rodPoints0 = DoubleMatrix.zeros(numrods,numpoints);
		rodPoints1 = DoubleMatrix.zeros(numrods,numpoints);
		rodMuscles = DoubleMatrix.zeros(numrods,1);
		int numjoints = joints.size();
		jointValues = DoubleMatrix.zeros(numjoints,2);
		jointPoints0 = DoubleMatrix.zeros(numjoints, numpoints);
		jointPoints1 = DoubleMatrix.zeros(numjoints, numpoints);
		jointPoints2 = DoubleMatrix.zeros(numjoints, numpoints);
		jointMuscles = DoubleMatrix.zeros(numjoints,1);
		
		for(int i = 0; i < numrods; i++) {
			Rod r = rods.get(i);
			rodValues.put(i,0,r.getRestValue1());
			rodValues.put(i,1,r.getRestValue2());
			rodPoints0.put(i,pointmasses.indexOf(r.getPoint0()),1);
			rodPoints1.put(i,pointmasses.indexOf(r.getPoint1()),1);
		}
		
		for(int i = 0; i < numjoints; i++) {
			Joint j = joints.get(i);
			jointValues.put(i,0,j.getRestValue1());
			jointValues.put(i,1,j.getRestValue2());
			jointPoints0.put(i,pointmasses.indexOf(j.getPoint0()),1);
			jointPoints1.put(i,pointmasses.indexOf(j.getPoint1()),1);
			jointPoints2.put(i,pointmasses.indexOf(j.getPoint2()),1);
		}
		
		for(int i = 0; i < numpoints; i++) {
			mass.put(i,0,pointmasses.get(i).getMass());
		}
		massRec = Util.recip(mass);
		
		rodPoints0Tran = rodPoints0.transpose();
		rodPoints1Tran = rodPoints1.transpose();
		jointPoints0Tran = jointPoints0.transpose();
		jointPoints1Tran = jointPoints1.transpose();
		jointPoints2Tran = jointPoints2.transpose();
		
//		brain = new Brain(senses, muscles);
		double sumlen = 0.5*rodValues.sum();
		double meanhalflen = sumlen / numrods / 2;
		double angle_delta = 2 * Math.PI / numpoints;
		for(int i = 0; i < numrods; i++){
			pos.put(i,0, x + Math.cos(i*angle_delta)*meanhalflen);
			pos.put(i,1,y + Math.sin(i*angle_delta)*meanhalflen);
		}
		for(int i=0; i<5; i++) {
			physicsUpdate();
			move(1.0);
		}
		vel.muli(0);
		acc.muli(0);
		force.muli(0);
		
//		updateHitBox();
	}
	
//	public void physicsUpdate(double dt){
//		brain.update();
//		// distribute energy between muscles
//		for(Muscle m : muscles)
//			energy -= m.act();
//		for(Joint j : joints)
//			j.physicsUpdate(theEnvironment);
//		for(Rod r : rods)
//			r.physicsUpdate(theEnvironment);
//		
//		// move point-mass-joints, update center-x and center-y coordinates
//		double sx = 0.0, sy = 0.0;
//		for(PointMass j : pointmasses){
//			j.move(theEnvironment, dt);
//			sx += j.getX();
//			sy += j.getY();
//		}
//		x = sx / pointmasses.size();
//		y = sy / pointmasses.size();
//		
//		radius = 0;
//		double dx, dy;
//		for(PointMass p : pointmasses) {
//			dx = p.getX() - x;
//			dy = p.getY() - y;
//			radius = Math.max(radius, (dx)*(dx) + (dy)*(dy));
//		}
//		radius = Math.sqrt(radius);
//	}
	
	public void physicsUpdate() {
		doRods();
		doJoints();
		if(selfCollisions) doCollisions();
		
		// PointMass friction
		force.subi(vel.mul(1));
	}
	
	public void move(double dt) {
		// Add in forces.
		acc.addi(force.mulColumnVector(massRec));
		force.mul(0);
		
		// TODO Cap speed.
		DoubleMatrix overSpeed = speed.gt(theEnvironment.MAX_SPEED);
		vel = vel.mulColumnVector(overSpeed.not()).add(vel.mulColumnVector(overSpeed).mulColumnVector(Util.recip(speed)).mul(theEnvironment.MAX_SPEED));
		
		// Update pos and vel.
		pos.addi(vel.mul(dt).add(acc.mul(0.5*dt*dt)));
		vel.addi(acc.mul(dt));
		acc.muli(0);
		speed = Util.mag(vel);
		
		// Update center of mass and radius.
		DoubleMatrix centerOfMass = pos.mulColumnVector(mass);
		centerOfMass = centerOfMass.columnSums();
		centerOfMass.divi(mass.sum());
		x = centerOfMass.get(0,0);
		y = centerOfMass.get(0,1);
		radius = Util.mag(pos.subiRowVector(centerOfMass)).max();
	}
	
	private void doRods() {
		// Restoring force.
		DoubleMatrix relPos = rodPoints1.mmul(pos).sub(rodPoints0.mmul(pos));
		DoubleMatrix relDist = Util.mag(relPos);
		DoubleMatrix relPosU = relPos.mulColumnVector(Util.recip(relDist));
		DoubleMatrix resForce = Util.plat(relDist,rodValues);
		resForce = resForce.muli(Rod.FORCE_PER_DISPLACEMENT);
		resForce = relPosU.mulColumnVector(resForce);
		
		// Restoring frictional force.
		DoubleMatrix relVel = rodPoints1.mmul(vel).sub(rodPoints0.mmul(vel));
		DoubleMatrix radVelComponent = relPosU.mulColumnVector(Util.dot(relVel,relPosU));
		DoubleMatrix fricForce = radVelComponent.mul(Rod.SPRING_FRICTION_CONSTANT);
		
		// Add forces back in.
		force.addi(rodPoints0Tran.mmul(resForce.add(fricForce)));
		force.subi(rodPoints1Tran.mmul(resForce.add(fricForce)));
	}
	
	private void doJoints() {
		// TODO
	}
	
	private void doCollisions() {
		// TODO
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
		// Prepare matrices to draw from.
		DoubleMatrix rodPoints0Pos = rodPoints0.mmul(pos);
		DoubleMatrix rodPoints1Pos = rodPoints1.mmul(pos);
		int n = rods.size();
		
		glColor4f(0f, 0.4f, 1.0f, 1.0f);
		for(int i = 0; i < n; i++) {
			glBegin(GL_LINES);
			{
				glVertex2d(rodPoints0Pos.get(i,0), rodPoints0Pos.get(i,1));
				glVertex2d(rodPoints1Pos.get(i,0), rodPoints1Pos.get(i,1));
			}
			glEnd();
		}
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
//	public Muscle getFirstMuscle() { return muscles.get(0); }
	
	public double getRadius() { return radius; }
	
}
