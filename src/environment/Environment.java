package environment;

import graphics.IDrawable;
import graphics.opengl.IDrawableGL;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

import physics.Joint;
import physics.PointMass;
import physics.Rod;
import structure.Organism;
import structure.OrganismFactory;

public class Environment implements IDrawable, IDrawableGL {
	
	public static final double GRAVITY = 0.1;
	public static final double MOUSE_CONSTANT = 1;
	public static final double MAX_SPEED = 10;

	public static final double ROD_FORCE_PER_DISPLACEMENT = 0.5;
	public static final double ROD_SPRING_FRICTION_CONSTANT = 0.5;
	
	public double viscosity;
	public double friction;
	public List<Organism> organisms;
	private int width, height;
	
	private int[] mouse_in;
	private int[] mouse_buttons;
	private int mousex, mousey;
	private boolean spaceIsPressed;
	
	public Environment(int w, int h){
		viscosity = 0.002;
		friction = 0.0;
		organisms = new LinkedList<Organism>();
		// DEBUGGING
		organisms.add(OrganismFactory.testDummy(OrganismFactory.SIMPLE_JELLYFISH,this));
		width = w;
		height = h;
	}
	
	public void update(double dt){
		mousex = mouse_in[0];
		mousey = mouse_in[1];
	
		dt /= 100.0;
		for(Organism o : organisms){
			//o.drift(0, -GRAVITY);
			o.physicsUpdate(dt > 1.0 ? 1.0 : dt);
			o.contain(this);
			
			 if(mouse_buttons[0] != 0) {
			 	double dist = Math.sqrt((mousex - o.getX())*(mousex - o.getX()) + (mousey - o.getY())*(mousey - o.getY()));
//			 	o.drift((mousex - o.getX()) / dist, (mousey - o.getY())/ dist);
			 	o.getPoints().get(0).addForce(MOUSE_CONSTANT*(mousex - o.getX()) / dist, MOUSE_CONSTANT*(mousey - o.getY())/ dist);
			 	System.out.println("Mouse down on: x = " + mousex + ", y = " + mousey + ".");
			 }
			 
			 try {
				 if(spaceIsPressed)
					 o.getFirstMuscle().setStrength(-1);
				 else
					 o.getFirstMuscle().setStrength(0.2);
			 } catch (NullPointerException e) {
			 }
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
		return new double[] {-width/2, -height/2, width/2, height/2};
	}
	
	public void mouse_move(int mx, int my){
		mouse_buttons[0] = 1;
		mousex = mx;
		mousey = my;
	}
	
	public void space_press(boolean isPressed) {
		spaceIsPressed = isPressed;
	}

	public void bindInput(int[] mouse_buttons, int[] mouse_move) {
		this.mouse_buttons = mouse_buttons;
		this.mouse_in = mouse_move;
	}
	
	// MatrixPhysics handles everything having to do with physical movement
	// through the environment.
	// 
	// MatrixPhysics should NOT handle energy, drawing, nor neural networks.
	private class MatrixPhysics {
		
		// Matrices containing physical quantities.
		private DoubleMatrix pos, vel, acc, force, posU, velU;
		private DoubleMatrix mass, massRec, dist, speed, speedRec, distRec;
		private DoubleMatrix rods, joints, rodDamage, jointDamage;
		
		// Book keeping lists.
		private List<Organism> organismList;
		private HashMap<Organism,Integer> organismMap;
		private List<PointMass> pointList;
		private HashMap<PointMass,Integer> pointMap;
		private List<Rod> rodList;
		private HashMap<Rod,Integer> rodMap;
		private List<Joint> jointList;
		private HashMap<Joint,Integer> jointMap;
		private DoubleMatrix rodIndices;
		private DoubleMatrix jointIndices;
		
		private boolean collisionsOn;
		
		private MatrixPhysics() {
			
			pos = new DoubleMatrix();
			vel = new DoubleMatrix();
			acc = new DoubleMatrix();
			force = new DoubleMatrix();
			
			mass = new DoubleMatrix();
			
			rods = new DoubleMatrix();
//			rodIndices = new DoubleMatrix();
			joints = new DoubleMatrix();
//			jointIndices = new DoubleMatrix();
			
			organismMap = new HashMap<Organism,Integer>();
			pointMap = new HashMap<PointMass,Integer>();
			rodMap = new HashMap<Rod,Integer>();
			jointMap = new HashMap<Joint,Integer>();
			
			collisionsOn = true;
		}
		
		// This constructor is called in addOrganism(Organism o)
		public MatrixPhysics(Organism o) {
			this();
			collisionsOn = false;
			
			// Initialize organism. Put points around a circle.
			pointList.addAll(o.points);
			organismMap.put(o, 0);
			int num = pointList.size();
			double theta = 2*Math.PI/(double)num;
			double radius = 30;
			pos = DoubleMatrix.zeros(num,2);
			vel = DoubleMatrix.zeros(num,2);
			acc = DoubleMatrix.zeros(num,2);
			force = DoubleMatrix.zeros(num,2);
			mass = DoubleMatrix.zeros(num,1);
			
			for(int i = 0; i < num; i++) {
				pos.put(i,0,radius*Math.cos(i*theta));
				pos.put(i,1,radius*Math.sin(i*theta));
				mass.put(i,0,pointList.get(i).getMass());
				pointMap.put(pointList.get(i),i);
			}
			
			// Add rods.
			rodList.addAll(o.rods);
			num = rodList.size();
			rods = DoubleMatrix.zeros(num,2);
			rodIndices = DoubleMatrix.zeros(num,2);
			
			for(int i = 0; i < num; i++) {
				Rod r = rodList.get(i);
				double v0 = r.getRestValue0();
				double v1 = r.getRestValue1();
				rods.put(i,0,Math.min(v0,v1));
				rods.put(i,0,Math.max(v0,v1));
				rodIndices.put(i,0, pointMap.get(r.point0));
				rodIndices.put(i,1, pointMap.get(r.point1));
				rodMap.put(r,i);
			}
			
			// Add joints.
			jointList.addAll(o.getJoints());
			num = jointList.size();
			joints = DoubleMatrix.zeros(num,2);
			jointIndices = DoubleMatrix.zeros(num,3);
			
			for(int i = 0; i < num; i++) {
				Joint j = jointList.get(i);
				double v0 = j.getRestValue0();
				double v1 = j.getRestValue1();
				joints.put(i,0,v0);
				joints.put(i,1,v1);
				jointIndices.put(i,0,pointMap.get(j.getPoint0()));
				jointIndices.put(i,1,pointMap.get(j.getPoint1()));
				jointIndices.put(i,2,pointMap.get(j.getPoint2()));
				jointMap.put(j,i);
			}
			
		}
		
		// Ticks the physics.
		public void update(double dt) {
			
			doRods();
			doJoints();
			if(collisionsOn) doCollisions();
			doBoundaries();
			
			// Cap speed at MAX_SPEED.
			DoubleMatrix isOverSpeed = speed.gt(MAX_SPEED);
			vel.muli((velU.mul(MAX_SPEED).mulColumnVector(isOverSpeed)).add(vel.mulColumnVector(isOverSpeed.not())));
			
			// Update pointmasses
			pos.addi( vel.mul(dt).add(acc.mul(0.5*dt*dt)) );
			vel.addi( acc.mul(dt) );
			acc.muli(0);
		}
		
		// Removes organism from physics.
		private void removeOrganism(Organism o) {
			
			// Prepare to remove shit.
			DoubleMatrix pointsToRemove = DoubleMatrix.zeros(pointList.size(),1);
			DoubleMatrix rodsToRemove = DoubleMatrix.zeros(rodList.size(),1);
			DoubleMatrix jointsToRemove = DoubleMatrix.zeros(jointList.size(),1);
			for(Joint j : o.joints) { jointsToRemove.put(jointMap.get(j), 0, 1); }
			for(Rod r : o.rods) { rodsToRemove.put(rodMap.get(r), 0, 1); }
			for(PointMass p : o.points) { pointsToRemove.put(pointMap.get(p),0,1); }
			
			// Remove some shit.
			joints = removeRows(joints,jointsToRemove);
			jointIndices = removeRows(jointIndices,jointsToRemove);
			rods = removeRows(rods, rodsToRemove);
			rodIndices = removeRows(rodIndices, rodsToRemove);
			pos = removeRows(pos, pointsToRemove);
			vel = removeRows(vel, pointsToRemove);
			acc = removeRows(acc, pointsToRemove);
			force = removeRows(force, pointsToRemove);
			mass = removeRows(mass, pointsToRemove);
			for(int i = jointList.size()-1; i >= 0; i--) {
				if(jointsToRemove.get(i,0) != 0) {
					jointList.remove(i);
				}
			}
			for(int i = rodList.size() - 1; i >= 0; i--) {
				if(rodsToRemove.get(i,0) != 0) {
					rodList.remove(i);
				}
			}
			for(int i = pointList.size()-1; i>= 0; i--) {
				if(pointsToRemove.get(i,0) != 0) {
					pointList.remove(i);
				}
			}
			
			// Reorganize some shit.
			// Reorganize the pointMap.
			int n = pointList.size();
			for(int i = 0; i < n; i++) {
				pointMap.put(pointList.get(i),i);
			}
			// Reorganize rodMap and rodIndices.
			n = rodList.size();
			for(int i = 0; i < n; i++) {
				Rod r = rodList.get(i);
				rodIndices.put(i,0,pointMap.get(r.point0));
				rodIndices.put(i,1,pointMap.get(r.point1));
				rodMap.put(r,i);
			}
			// Reorganize jointMap and jointIndices.
			n = jointList.size();
			for(int i = 0; i < n; i++) {
				Joint j = jointList.get(i);
				jointIndices.put(i,0, pointMap.get(j.getPoint0()));
				jointIndices.put(i,1, pointMap.get(j.getPoint1()));
				jointIndices.put(i,2, pointMap.get(j.getPoint2()));
				jointMap.put(j, i);
			}
		}
		
		// Adds and initializes organism.
		private void addOrganism(Organism o) {
			
			// Initialize o by putting it in its own physics and iterate without intersection.
			MatrixPhysics oPhys = new MatrixPhysics(o);
			for(int i = 0; i < 6; i++) {
				oPhys.update(1);
			}
			oPhys.vel.mul(0);
			oPhys.acc.mul(0);
			
			// TODO Delay the next part until the organism has free space to spawn in.
			
			// Add it to the normal physics.
			// Add points.
			int n = pointList.size();
			pointList.addAll(oPhys.pointList);
			for(int i = n; i < pointList.size(); i++) {
				pointMap.put(pointList.get(i),i);
			}
			pos = DoubleMatrix.concatVertically(pos, oPhys.pos);
			vel = DoubleMatrix.concatVertically(vel, oPhys.vel);
			acc = DoubleMatrix.concatVertically(acc, oPhys.acc);
			force = DoubleMatrix.concatVertically(force, oPhys.force);
			mass = DoubleMatrix.concatVertically(mass, oPhys.mass);
			
			// Add rods.
			n = rodList.size();
			rodList.addAll(oPhys.rodList);
			for(int i = n; i < rodList.size(); i++) {
				rodMap.put(rodList.get(i), i);
			}
			rods = DoubleMatrix.concatVertically(rods, oPhys.rods);
			rodIndices = DoubleMatrix.concatVertically(rodIndices, oPhys.rodIndices);
			// For loop may be able to be replaced with the line rodIndices.addi(n);
			for(int i = n; i < rodList.size(); i++) {
				Rod r = rodList.get(i);
				rodIndices.put(i,0,pointMap.get(r.point0));
				rodIndices.put(i,1,pointMap.get(r.point1));
			}
			
			// Add joints.
			n = jointList.size();
			jointList.addAll(oPhys.jointList);
			for(int i = n; i < jointList.size(); i++) {
				jointMap.put(jointList.get(i), i);
			}
			joints = DoubleMatrix.concatVertically(joints,  oPhys.joints);
			jointIndices = DoubleMatrix.concatVertically(jointIndices, oPhys.jointIndices);
			// For loop may be able to be replaced with the line jointIndices.addi(n);
			for(int i = n; i < jointList.size(); i++) {
				Joint j = jointList.get(i);
				jointIndices.put(i,0,pointMap.get(j.getPoint0()));
				jointIndices.put(i,1,pointMap.get(j.getPoint1()));
				jointIndices.put(i,2,pointMap.get(j.getPoint2()));
			}
		}
		
		// Calculates forces having to do with rods, but not collisions.
		private void doRods() {
			// Restoring forces.
			int n = rodList.size();
			DoubleMatrix p0 = DoubleMatrix.zeros(n,2);
			DoubleMatrix p1 = DoubleMatrix.zeros(n,2);
			DoubleMatrix v0 = DoubleMatrix.zeros(n,2);
			DoubleMatrix v1 = DoubleMatrix.zeros(n,2);
			DoubleMatrix f0 = DoubleMatrix.zeros(n,2);
			DoubleMatrix f1 = DoubleMatrix.zeros(n,2);
			DoubleMatrix relDist;
			DoubleMatrix relPosU;
			DoubleMatrix relPos;
			DoubleMatrix relVel;
			DoubleMatrix relVelU;
			DoubleMatrix relSpeed;
			DoubleMatrix strain;
			// Calculate restoring force.
			// Replace this step with sparse matrix multiplication when supported.
			for(int i = 0; i < n; i++) {
				p0.putRow(i,pos.getRow((int)rodIndices.get(i,0)));
				p1.putRow(i,pos.getRow((int)rodIndices.get(i,1)));
				v0.putRow(i,vel.getRow((int)rodIndices.get(i,0)));
				v1.putRow(i,vel.getRow((int)rodIndices.get(i,1)));
			}
			relPos = p1.sub(p0);
			relDist = mag(relPos);
			relPosU = relPos.mul(recip(relDist));
			relVel = v1.sub(p1);
			relSpeed = mag(relVel);
			relVelU = relVel.mul(recip(relSpeed));
			strain = relPosU.mulColumnVector((plat(relDist,rods).mul(-ROD_FORCE_PER_DISPLACEMENT)));
			f0.addi(strain);
			f1.subi(strain);
			
			// TODO Calculate frictional force.
			
			// Add forces to points.
			
		}
		
		// Calculates forces having to do with joints.
		private void doJoints() {
			// TODO this
		}
		
		// Calculates forces resulting form collisions between rods.
		private void doCollisions() {
			
			// TODO Code for collisions
		}
		
		// Calculates force from environment boundaries.
		private void doBoundaries() {
			// TODO this
		}
		
		// Returns a new matrix equal to m minus the rows corresponding to nonzero elements of toRemove.
		private DoubleMatrix removeRows(DoubleMatrix m, DoubleMatrix toRemove) {
			if(!toRemove.isColumnVector()) {
				System.out.println("Fuckup on aisle removeRows(DoubleMatrix, DoubleMatrix).");
				System.exit(0);
			}
			DoubleMatrix toKeep = toRemove.not();
			int newRows = (int)toKeep.sum();
			DoubleMatrix holder = DoubleMatrix.zeros(newRows, m.columns);
			int j = 0;
			for(int i = 0; j < newRows; i++) {
				holder.putRow(j,m.getRow(i));
				j += (int)toKeep.get(i);
			}
			return holder;
		}
		
		// Returns magnitude
		private DoubleMatrix mag(DoubleMatrix m) {
			return MatrixFunctions.sqrt((m.mul(m)).rowSums());
		}
		
		// Does 1/x entry by entry, except it gives 0 when x = 0.
		private DoubleMatrix recip(DoubleMatrix m) {
			DoubleMatrix zero = m.not();
			return (zero.not()).div(m.add(zero));
		}
		
		// Applies the function f entry-wize to the matrix m, where
		// f is a monotonic, continuous function which plateaus from a to b.
		private DoubleMatrix plat(DoubleMatrix m, DoubleMatrix c) {
			DoubleMatrix a = c.getColumn(0);
			DoubleMatrix b = c.getColumn(1);
			return ((m.mul(m.lt(a))).sub(a)).add(m.mul(m.gt(b)).sub(b));
		}
		
	}


}
