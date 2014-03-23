package utils.grid;

import java.util.HashSet;

import bio.organisms.AbstractOrganism;

public class Chunk extends HashSet<AbstractOrganism> {
	
	private static final long serialVersionUID = -1953294672491456887L;

	public static final int SIZE = 20; 
	
	private int x;
	private int y;
	private HashCoords coords;
	
	public Chunk(int x, int y) {
		this.x = x;
		this.y = y;
		coords = new HashCoords();
		coords.set(x,y);
	}
	
	public int[] getGridCoords() {
		int[] coords = new int[2];
		coords[0] = x;
		coords[1] = y;
		return coords;
	}
	
	public int[] getWorldCoords() {
		int[] coords = new int[2];
		coords[0] = x*SIZE;
		coords[1] = y*SIZE;
		return coords;
	}
	
	public int getGridX() {
		return x;
	}
	
	public int getGridY() {
		return y;
	}
	
	public int getWorldX() {
		return x*SIZE;
	}
	
	public int getWorldY() {
		return y*SIZE;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(!(o instanceof Chunk)) {
			return false;
		}
		
		return (x == ((Chunk)o).x) && (y == ((Chunk)o).y);
	}
	
	@Override
	public int hashCode() {
		return coords.hashCode(); // This is likely bad practice...but suck it?
	}
	
	@Override
	public String toString(){
		return "Chunk @("+x+","+y+"): "+super.toString();
	}

}
