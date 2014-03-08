package neuralevo;

import java.util.LinkedList;

public class Square extends LinkedList<NOrganism> {

	private static final long serialVersionUID = -6523984801684487314L;

	public static final int SIZE = 8*initializeSize();
	
	private int x;
	private int y;
	
	public Square(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Square(int[] indices) {
		x = indices[0];
		y = indices[1];
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int[] getIndices() {
		return new int[] {x,y};
	}
	
	private static int initializeSize() {
		int s = 1;
		while(s < NOrganism.RANGE)
			s *= 2;
		return s;
	}

}
