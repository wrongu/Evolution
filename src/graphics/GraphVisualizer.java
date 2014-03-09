package graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import org.jblas.DoubleMatrix;

public class GraphVisualizer {

	public static double EASE_SPRING = 10000D; 
	public static int point_half_width = 5;
	public static Color PRIMARY_COLOR = Color.BLACK;
	public static double MIN_LENGTH = 10D;
	public static double MAX_LENGTH = 300D;

	public static void drawGraphSpring(Graphics2D g, DoubleMatrix adjacency_matrix, Rectangle bounds, int n_init){
		adjacency_matrix.assertSquare();

		double nmax = adjacency_matrix.normmax();
		// TODO - do the whole thing n_init times and take the best one

		// random initialization of coordinates
		Point2d[] coords = new Point2d[adjacency_matrix.rows];
		Point2d[] nextcoords = new Point2d[adjacency_matrix.rows];
		for(int i = 0; i < coords.length; i++){
			double randx = (double) (Math.random() * bounds.width);
			double randy = (double) (Math.random() * bounds.height);
			coords[i] = new Point2d(randx, randy);
			nextcoords[i] = new Point2d(randx, randy);
		}

		double maxmove2 = 2D;
		// loop until convergence
		while(maxmove2 > .1D){
			// update nextcoords from coords
			//			System.out.println("updating nextcoords");
			for(int i = 0; i < coords.length; i++){
				for(int j = 0; j < coords.length; j++){
					double weight = Math.abs(adjacency_matrix.get(i, j)); 
//					if(weight != 0.0){
						double dx = coords[j].x - coords[i].x;
						double dy = coords[j].y - coords[i].y;
						double ideal_length = MIN_LENGTH + (1 - weight / nmax) * MAX_LENGTH;

						// positive strain means it's too long and will try to shrink
						double strain = (coords[i].distTo(coords[j]) - ideal_length) / weight;
						
						// incrementally adjust points in the direction that reduces strain
						nextcoords[i].x += dx * strain / EASE_SPRING;
						nextcoords[i].y += dy * strain / EASE_SPRING;

						nextcoords[j].x -= dx * strain / EASE_SPRING;
						nextcoords[j].y -= dy * strain / EASE_SPRING;
//					}
				}
			}

			// update positions
			maxmove2 = 0F;
			for(int i=0; i<coords.length; i++){
				double movedist2 = (coords[i].x - nextcoords[i].x)*(coords[i].x - nextcoords[i].x) + 
						(coords[i].y - nextcoords[i].y)*(coords[i].y - nextcoords[i].y);
				if(movedist2 > maxmove2) maxmove2 = movedist2;
				coords[i] = nextcoords[i];
				nextcoords[i] = new Point2d(coords[i].x, coords[i].y);
			}

			// BEGIN DEBUG
			System.out.println(maxmove2);
			// END DEBUG
		}

		// TODO - choose best result from n_init options

		double minx, maxx, miny, maxy;
		minx = maxx = coords[0].x;
		miny = maxy = coords[0].y;
		for(int i=1; i < coords.length; i++){
			if(coords[i].x < minx) minx = coords[i].x;
			if(coords[i].x > maxx) maxx = coords[i].x;
			if(coords[i].y < miny) miny = coords[i].y;
			if(coords[i].y > maxy) maxy = coords[i].y;
		}
		double xshift = -minx;
		double xscale = bounds.width / (maxx - minx);
		double yshift = -miny;
		double yscale = bounds.height / (maxy - miny);

		Point[] finalcoords = new Point[coords.length];
		for(int i = 0; i < coords.length; i++){
			finalcoords[i] = new Point((int) ((coords[i].x + xshift) * xscale + bounds.x),
					(int) ((coords[i].y + yshift) * yscale + bounds.y));
		}

		drawGraph(g, adjacency_matrix, finalcoords);
	}

	public static void drawGraph(Graphics2D g, DoubleMatrix adjacency_matrix, Point[] finalcoords){

		double nmax = adjacency_matrix.normmax();

		for(int i = 0; i < finalcoords.length; i++){
			for(int j = 0; j < finalcoords.length; j++){
				// draw (i,j)th connection
				double weight = Math.abs(adjacency_matrix.get(i,j));
				if(weight != 0.0){
					g.setColor(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(),
							(int) (10 + 245 * weight / nmax)));
					g.drawLine(finalcoords[i].x, finalcoords[i].y, finalcoords[j].x, finalcoords[j].y);
				}
			}
			// draw ith point
			g.setColor(PRIMARY_COLOR);
			g.fillOval(finalcoords[i].x - point_half_width, finalcoords[i].y - point_half_width,
					2*point_half_width, 2*point_half_width);
		}
	}

	public static void drawGraphSpring(Graphics2D g, DoubleMatrix adjacency_matrix, Rectangle bounds){
		drawGraphSpring(g, adjacency_matrix, bounds, 10);
	}

	public static void drawGraphEigen(Graphics2D g, DoubleMatrix adjacency_matrix, Rectangle bounds){
		adjacency_matrix.assertSquare();

	}

	private static class Point2d{
		public double x,  y;

		public Point2d(double x, double y){
			this.x = x;
			this.y = y;
		}

		public double distTo(Point2d other){
			double dx = x - other.x;
			double dy = y - other.y;
			return Math.sqrt(dx*dx + dy*dy);
		}
	}
}
