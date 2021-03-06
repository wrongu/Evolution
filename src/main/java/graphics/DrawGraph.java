package graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.jblas.DoubleMatrix;


public class DrawGraph implements IDrawable {

	private DoubleMatrix mat;

	public DrawGraph(){
		int n = 20;
		mat = new DoubleMatrix(n, n);
		// group 1
		for(int i = 0; i < n*n / 7; i++){
			int i1 = (int) (Math.random()*n/2);
			int i2 = (int) (Math.random()*n/2);
			mat.put(i1, i2, Math.random());
		}
		// group 2
		for(int i = 0; i < n*n / 7; i++){
			int i1 = (int) (Math.random()*n/2 + n/2);
			int i2 = (int) (Math.random()*n/2 + n/2);
			mat.put(i1, i2, Math.random());
		}
		// cross connections
		for(int i = 0; i < n*n / 14; i++){
			int i1 = (int) (Math.random()*n/2);
			int i2 = (int) (Math.random()*n/2 + n/2);
			mat.put(i1, i2, Math.random() / 4);
		}
		System.out.println(mat);
		GraphVisualizer.PRIMARY_COLOR = Color.WHITE;
	}

	public void draw(Graphics2D g, float shiftx, float shifty, float scalex, float scaley) {
		GraphVisualizer.drawGraphSpring((Graphics2D) g, mat, new Rectangle(10+(int)shiftx, 10+(int)shifty,
				(int) (300 * scalex), (int) (400 * scaley)));
	}
}
