package physics;

// import java.util.HashMap;
import java.util.HashMap;
import java.util.LinkedList;
// import java.lang.Math;





import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

import structure.Organism;
import environment.Environment;

public class MatrixPhysics {
	
	// Constants
	public static double MAX_SPEED = 10;
	
	// Matrices and stuff
	private Environment env;
	private DoubleMatrix pos, vel, acc, force, posU, velU;
	private DoubleMatrix rod; // n x 5 matrices, [0] is (1 if rod, 0 if not), [1] is minLength, [2] is maxLength.
	private DoubleMatrix mass, massRec, dist, speed, speedRec, distRec;
	private DoubleMatrix rods, joints;
	
	// Book keeping. This keeps track of what points, rods, and joints belong to which organisms.
	private LinkedList<Organism> jointOrganism, rodOrganism, pointOrganism;
	private HashMap <Organism, Integer> organismPoint, organismRod, organismJoint;
	
	public MatrixPhysics(Environment e) {
		if(e == null) {
			System.out.println("You fucked up a MatrixPhysics constructor.");
			System.exit(0);
		}
		env = e;
		
		pos = new DoubleMatrix();
		vel = new DoubleMatrix();
		acc = new DoubleMatrix();
		rods = new DoubleMatrix();
		joints = new DoubleMatrix(); 
		mass = new DoubleMatrix();
	}
	
	// Adds the organism and returns the index. Call it at birth. Iterates the physics.
	public void addOrganism(Organism o) {
		
	}
	
	// Removes the organism and all physical structures associated with it.
	public void removeOrganism(Organism o) {
		
	}

	public void update(double dt) {
		// Update these heavy-ass matrices in one place so everything else can run faster.
		speed = mag(vel);
		dist = mag(pos);
		speedRec = recip(speed);
		distRec = recip(dist);
		
		doRods(); // Perhaps per organism. Then viscosity is done all together.
		doJoints(); // Again, per organism.
		doCollisions(); // All together.
		
		// Calculate acceleration from forces.
		acc.addi(force.divColumnVector(mass));
		
		// Cap speed at MAX_SPEED.
		DoubleMatrix isOverSpeed = speed.gt(MAX_SPEED);
		vel.muli((velU.mul(MAX_SPEED).mulColumnVector(isOverSpeed)).add(vel.mulColumnVector(isOverSpeed.not())));
		
		// Update position, velocity, and set acceleration to 0.
		pos.addi((vel.mul(dt)).add(acc.mul(0.5*dt*dt)));
		vel.addi(acc.mul(dt));
		acc.muli(0);
	}
	
	public void update(double dt, Organism o) {
		speed = mag(vel);
		dist = mag(pos);
		speedRec = recip(speed);
		distRec = recip(dist);
		
		doRods(o);
		doJoints(o);
		
		// Calculate acceleration from forces.
		acc.addi(force.divColumnVector(mass));
		
		// Cap speed at MAX_SPEED.
		DoubleMatrix isOverSpeed = speed.gt(MAX_SPEED);
		vel.muli((velU.mul(MAX_SPEED).mulColumnVector(isOverSpeed)).add(vel.mulColumnVector(isOverSpeed.not())));
		
		// Update position, velocity, and set acceleration to 0.
		pos.addi((vel.mul(dt)).add(acc.mul(0.5*dt*dt)));
		vel.addi(acc.mul(dt));
		acc.muli(0);
	}
	
	private void doJoints(Organism o) {
		// TODO Auto-generated method stub
		
	}

	private void doRods(Organism o) {
		// TODO Auto-generated method stub
		
	}

	private void doRods() {
		// Spring force
		
		
		// Spring friction
	}
	
	private void doJoints() {
		
	}
	
	private void doCollisions() {
		
	}
	
	// Returns the matrix n(i,j) = sqrt(m[0](i,j)^2 + m[1](i,j)^2).
	private static DoubleMatrix mag(DoubleMatrix m) {
		return MatrixFunctions.sqrt((m.mul(m)).rowSums());
	}
	
	// Applies the function f entry-wize to the matrix m, where
	// f is a monotonic, continuous function which plateaus from a to b.
	private static DoubleMatrix plat(DoubleMatrix m, double a, double b) {
		if(b < a) {
			double c = b;
			b = a;
			a = c;
		}
		
		return ((m.mul(m.lt(a))).sub(a)).add(m.mul(m.gt(b)).sub(b));
	}
	
	// Returns the entry-wise maximum of the two matrices
	private static DoubleMatrix max(DoubleMatrix m, DoubleMatrix n) {
		DoubleMatrix mIsBigger = m.gt(n);
		return (mIsBigger.mul(n)).add((mIsBigger.not()).add(n));
	}
	
	// Does 1/x entry by entry, except it gives 0 when x = 0.
	private static DoubleMatrix recip(DoubleMatrix m) {
		DoubleMatrix zero = m.not();
		return (zero.not()).div(m.add(zero));
	}

	private static void printMatrix(DoubleMatrix m) {
		for(int i = 0; i < m.rows; i++) {
			System.out.print("[ ");
			for(int j = 0; j < m.columns; j++) {
				System.out.print((int) m.get(i,j) + " ");
			}
			System.out.println("]");
		}
	}
	
	// Removes all rows of m for which there is a nonzero element in toRemove
	private static DoubleMatrix removeRows(DoubleMatrix m, DoubleMatrix toRemove) {
		if(!toRemove.isColumnVector()) {
			System.out.println("Fuckup on aisle removeRows(DoubleMatrix, DoubleMatrix).");
			System.exit(0);
		}
		DoubleMatrix toKeep = toRemove.not();
		int newRows = (int)toKeep.sum();
		DoubleMatrix holder = DoubleMatrix.zeros(newRows, m.columns);
		m.muliColumnVector(toKeep);
		int j = 0;
		for(int i = 0; j < newRows; i++) {
			holder.putRow(j,m.getRow(i));
			j += (int)toKeep.get(i);
		}
		return holder;
	}
	
	public static void main(String args[]) {
		DoubleMatrix a = new DoubleMatrix(new double[][] {{0,1}, {2,3}, {4,5}, {6,7}, {8,9}} );
		DoubleMatrix remove = new DoubleMatrix(new double[][] {{1},{1},{1},{0},{0}});
		System.out.println("A = ");
		printMatrix(a);
		System.out.println("Remove = ");
		printMatrix(remove);
		System.out.println("removeRows(A,Remove) = ");
		a = removeRows(a,remove);
		printMatrix(a);
	}
}
