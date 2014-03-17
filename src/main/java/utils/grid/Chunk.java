package utils.grid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import bio.organisms.AbstractOrganism;

public class Chunk extends HashSet<AbstractOrganism> {
	
	private static final long serialVersionUID = -1953294672491456887L;

	public static final int SIZE = 20; // MUST be at least twice 
	
	private int x;
	private int y;
	
	public Chunk(int x, int y) {
		this.x = x;
		this.y = y;
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

}
