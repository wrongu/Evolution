package structure;
import javax.vecmath.Vector2d;
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;


public class Util {
	public static double cross(Vector2d A, Vector2d B){
		return (A.x*B.y - B.x*A.y);
	}
	
	public static DoubleMatrix dot(DoubleMatrix a, DoubleMatrix b) {
		return (a.mul(b)).rowSums();
	}
	
	public static DoubleMatrix cross(DoubleMatrix a, DoubleMatrix b) {
		return (a.mul(b.swapColumns(0,1).mulColumn(1,-1))).rowSums();
	}
	
	public static DoubleMatrix mag(DoubleMatrix a) {
		return MatrixFunctions.sqrt(a.mul(a).rowSums());
	}
	
	// Entry-wise reciprocal operator on a matrix, and which returns 0 on 0.
	public static DoubleMatrix recip(DoubleMatrix a) {
		DoubleMatrix isZero = a.not();
		return isZero.not().div(a.add(isZero));
	}
	
	// Entry-wise plateau function on column vector a, with b giving the cutoffs.
	public static DoubleMatrix plat(DoubleMatrix a, DoubleMatrix b) {
		DoubleMatrix b0 = b.getColumn(0);
		DoubleMatrix b1 = b.getColumn(1);
		return a.sub(b0).mul(a.lt(b0)).add(a.sub(b1).mul(a.gt(b1)));
	}
	
	public static void main(String[] args) {
		DoubleMatrix a = new DoubleMatrix(new double[][] {{-1},{0},{1},{2},{4}});
		DoubleMatrix b = new DoubleMatrix(new double[][] {{0,1},{0,1},{3,4},{3,4},{3,4}});
		System.out.println("Matrix A = ");
		System.out.println(a.toString());
		System.out.println("New A = ");
		System.out.println(plat(a,b).toString());
	}
}
