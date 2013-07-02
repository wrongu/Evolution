package graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPanel;

import org.jblas.DoubleMatrix;

public class RenderPanel extends JPanel {

	private static final long serialVersionUID = -1902890242410191035L;
	
	public final Color BACKGROUND_COLOR = Color.BLACK;
	
	private DoubleMatrix mat;
	
	public RenderPanel(){
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
		Visualizer.PRIMARY_COLOR = Color.WHITE;
	}

	@Override
	public void paintComponent(Graphics g){
		background(g);
		Visualizer.drawGraphSpring((Graphics2D) g, mat, new Rectangle(10, 10, this.getWidth()-20, this.getHeight()-20));
	}
	
	private void background(Graphics g){
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0,0,this.getWidth(), this.getHeight());
	}
}
