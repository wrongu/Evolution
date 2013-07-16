package physics;

import org.jblas.*;

import environment.Environment;

public class MatrixPhysics {
	
	private Environment env;
	private ComplexDoubleMatrix mass, pos, vel, acc;
	
	public MatrixPhysics(Environment e) {
		if(e == null) {
			System.out.println("You fucked up a MatrixPhysics constructor.");
			System.exit(0);
		}
		env = e;
	}
	
	

}
