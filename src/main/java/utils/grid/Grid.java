package utils.grid;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import bio.organisms.AbstractOrganism;
import bio.organisms.Entity;
import bio.organisms.SimpleCircleOrganism;

/**
 * A wrapper for a HashMap<HashCoords,Chunk> object. Contains
 * put and get methods for accessing the HashMap, only the
 * key is always a pair of integers, and the values are
 * Chunks.
 * 
 * The iterator() method returns the iterator for the
 * collection view of the values of the HashMap, so it is
 * possible to easily loop through the Chunks stored in the
 * Grid without knowing the coordinates.
 * 
 * @author ewy-man
 */
public class Grid<T extends Entity> implements Iterable<T> {
	
	private static final double ROOT_2 = Math.sqrt(2);
	
	private HashMap<HashCoords,Chunk> map;
	private HashCoords coords;
	private final int CHUNK_SIZE;
	
	public Grid(int chunkSize) {
		CHUNK_SIZE = chunkSize > 0 ? chunkSize : 1;
		map = new HashMap<HashCoords,Chunk>();
		coords = new HashCoords();
	}
	
	/**
	 * Loop through the HashMap and remove any mappings which
	 * map to null or an empty Chunk.
	 */
	public void removeEmpties() {
		
//		map.remove(null);
		for(Iterator<Chunk> i = map.values().iterator(); i.hasNext(); ) {
			Chunk c = i.next();
			if(c.isEmpty()) {
				i.remove();
			}
		}
	}
	
	/**
	 * Adds a new AbstractOrganism to the appropriate chunk.
	 * Creates a chunk if such a chunk does not exist.
	 * 
	 * @param orgo
	 */
	public void add(T orgo) {
		
		// Compute the appropriate chunk coordinates of the AbstractOrganism.
		int x = (int)(orgo.getX()/CHUNK_SIZE);
		int y = (int)(orgo.getY()/CHUNK_SIZE);
		coords.set(x,y);
		
		// Add organism to chunk.
		Chunk c = summonChunk(x,y);
		c.add(orgo);
	}
	
	/**
	 * Adds all AbstractOrganisms to their corresponding chunks.
	 * Any corresponding chunks which do not already exist are
	 * created.
	 * 
	 * @param orgs
	 */
	public void add(Collection<T> ents) {
		for(T e : ents) {
			int x = (int)(e.getX()/CHUNK_SIZE);
			int y = (int)(e.getY()/CHUNK_SIZE);
			coords.set(x, y);
			
			Chunk c = summonChunk(x,y);
			c.add(e);
		}
	}
	
	public int getCount(){
		int c = 0;
		for(Chunk ch : this.map.values())
			c += ch.size();
		return c;
	}
	
	public int getChunkCount() {
		return map.values().size();
	}
	
	/**
	 * Returns the Chunk at coordinates (x,y).
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Chunk get(int x, int y) {
		coords.set(x,y);
		return map.get(coords);
	}
	
	/**
	 * Clears all Chunks and Organisms from the Grid.
	 */
	public void clear() {
		map.clear();
	}
	
	/**
	 * Returned iterator iterates through the chunks in the grid.
	 * The remove() method is supported.
	 * 
	 * TODO: Change to iterating through organisms.
	 */
	public Iterator<T> iterator() {
		return new GridIterator();
	}
	
	/**
	 * Returns a LinkedList of all those AbstractOrganisms within a radius of r
	 * of the coordinates (x,y). The parameters x,y, and r are assumed to be in
	 * world coordinates, so no scaling is necessary.
	 */
	public LinkedList<T> getInDisk(double x, double y, double r) {
		
		x /= CHUNK_SIZE;
		y /= CHUNK_SIZE;
		r /= CHUNK_SIZE;
		double s = r + ROOT_2;
		
		int x_L = (int)Math.floor(x - s);
		int x_U = (int)Math.ceil(x + s);
		int y_L = (int)Math.floor(y - s);
		int y_U = (int)Math.ceil(y + s);
		int x_range = x_U - x_L + 1;
		int y_range = y_U - y_L + 1;
		boolean[][] markersR = new boolean[x_range][y_range];
		boolean[][] markersS = new boolean[x_range][y_range];
		
		LinkedList<T> ents = new LinkedList<T>();
		
		for(int i = 0; i < x_range; i++) {
			for(int j = 0; j < y_range; j++) {
				double dx = i + x_L - x;
				double dy = j + y_L - y;
				double radSqrd = dx*dx + dy*dy;
				markersR[i][j] = radSqrd < r*r;
				markersS[i][j] = radSqrd < s*s;
			}
		}
		
		for(int i = 0; i < x_range - 1; i++) {
			for(int j = 0; j < y_range - 1; j++) {
				
				coords.set(i + x_L, j + y_L);
				
				if(markersR[i][j] & markersR[i+1][j] & markersR[i][j+1] & markersR[i+1][j+1]) {
					Chunk c = map.get(coords);
					if(c != null)
						ents.addAll(c);
				} else if(markersS[i][j] & markersS[i+1][j] & markersS[i][j+1] & markersS[i+1][j+1]) {
					Chunk c = map.get(coords);
					if(c != null) {
						// Check individual organisms
						for(T e : c) {
							double dx = e.getX()/CHUNK_SIZE - x;
							double dy = e.getY()/CHUNK_SIZE - y;
							if(dx*dx + dy*dy < r*r) {
								ents.add(e);
							}
						}
					}
				}
			}
		}
		
		return ents;
		
	}
	
	public LinkedList<T> getInDiskMut(double x, double y, double r) {
		
		x /= CHUNK_SIZE;
		y /= CHUNK_SIZE;
		r /= CHUNK_SIZE;
		double s = r + ROOT_2;
		
		int x_L = (int)Math.floor(x - s);
		int x_U = (int)Math.ceil(x + s);
		int y_L = (int)Math.floor(y);
		int y_U = (int)Math.ceil(y + s);
		int x_range = x_U - x_L + 1;
		int y_range = y_U - y_L + 1;
		boolean[][] markersR = new boolean[x_range][y_range];
		boolean[][] markersS = new boolean[x_range][y_range];
		
		LinkedList<T> ents = new LinkedList<T>();
		
		for(int i = 0; i < x_range; i++) {
			for(int j = 0; j < y_range; j++) {
				double dx = i + x_L - x;
				double dy = j + y_L - y;
				double radSqrd = dx*dx + dy*dy;
				markersR[i][j] = radSqrd < r*r;
				markersS[i][j] = radSqrd < s*s;
			}
		}
		for(int i = 0 ; i < y_range; i++) {
			markersR[i][0] = false;
		}
		
		for(int i = 0; i < x_range - 1; i++) {
			for(int j = 0; j < y_range - 1; j++) {
				
				coords.set(i + x_L, j + y_L);
				
				if(markersR[i][j] & markersR[i+1][j] & markersR[i][j+1] & markersR[i+1][j+1]) {
					Chunk c = map.get(coords);
					ents.addAll(c);
				} else if(markersS[i][j] & markersS[i+1][j] & markersS[i][j+1] & markersS[i+1][j+1]) {
					Chunk c = map.get(coords);
					if(c != null) {
						// Check individual organisms
						for(T e : c) {
							double dx = e.getX()/CHUNK_SIZE - x;
							double dy = e.getY()/CHUNK_SIZE - y;
							if(dx*dx + dy*dy < r*r & ((dy > 0) | (dy == 0 & dx > 0)) ) {
								ents.add(e);
							}
						}
					}
				}
			}
		}
		
		return ents;
	}
	
	/**
	 * Returns the Entities within the bounding box (x_1,x_2) x (y_1,y_2),
	 * in interval notation.
	 * 
	 * @param x_1
	 * @param x_2
	 * @param y_1
	 * @param y_2
	 * @return entities
	 */
	public LinkedList<T> getInBox(double x_1, double x_2, double y_1, double y_2) {
		
		x_1 /= CHUNK_SIZE;
		x_2 /= CHUNK_SIZE;
		y_1 /= CHUNK_SIZE;
		y_2 /= CHUNK_SIZE;
		LinkedList<T> entities = new LinkedList<T>();
		
		if(x_1 > x_2) {
			double holder = x_2;
			x_2 = x_1;
			x_1 = holder;
		}
		if(y_1 > y_2) {
			double holder = y_2;
			y_2 = y_1;
			y_1 = holder;
		}
		
		for(int x = (int)Math.ceil(x_1); x < Math.ceil(x_2) - 1; x++) {
			for(int y = (int)Math.ceil(y_1); y < Math.ceil(y_2) - 1; y++) {
				
				coords.set(x,y);
				Chunk chunk = map.get(coords);
				if(chunk != null) { // && !chunk.isEmpty()
					entities.addAll(chunk);
				}
			}
		}
		
		HashSet<Chunk> toCheck = new HashSet<Chunk>();
		int x,y;
		
		x = (int)Math.ceil(x_1) - 1;
		for(y = (int)Math.ceil(y_1)-1; y < Math.ceil(y_2); y++) {
			coords.set(x,y);
			toCheck.add(map.get(coords));
		}
		
		x = (int)Math.ceil(x_2) - 1;
		for(y = (int)Math.ceil(y_1)-1; y < Math.ceil(y_2); y++) {
			coords.set(x,y);
			toCheck.add(map.get(coords));
		}
		
		y = (int)Math.ceil(y_1) - 1;
		for(x = (int)Math.ceil(x_1)-1; x < Math.ceil(x_2); x++) {
			coords.set(x,y);
			toCheck.add(map.get(coords));
		}
		
		y = (int)Math.ceil(y_2) - 1;
		for(x = (int)Math.ceil(x_1)-1; x < Math.ceil(x_2); x++) {
			coords.set(x,y);
			toCheck.add(map.get(coords));
		}
		
		toCheck.remove(null);
		for(Chunk c : toCheck) {
			for(T e : c) {
				double ex = e.getX()/CHUNK_SIZE;
				double ey = e.getY()/CHUNK_SIZE;
				if(ex < x_2 & ex > x_1 & ey < y_2 & ey > y_1) {
					entities.add(e);
				}
			}
		}
		
		return entities;
	}
	
	/**
	 * Checks which entities are in which chunks. Moves entities
	 * to different chunks if necessary.
	 */
	public void updateChunks() {
		LinkedList<T> toAdd = new LinkedList<T>();
		
		for(Chunk c : map.values()) {
			for(Iterator<T> i = c.iterator(); i.hasNext(); ) {
				T o = i.next();
				int o_x = (int)(o.getX()/CHUNK_SIZE);
				int o_y = (int)(o.getY()/CHUNK_SIZE);
				if(!(o_x == c.getGridX() && o_y == c.getGridY())) {
					toAdd.add(o);
					i.remove();
				}
			}
		}
		
		for(T o : toAdd) {
			this.add(o);
		}
	}
	
	/**
	 * Returns the chunk at coordinates (x,y).
	 * Creates the chunk if none exists.
	 */
	private Chunk summonChunk(int x, int y) {
		coords.set(x,y);
		Chunk chunk = map.get(coords);
		if(chunk == null) {
			chunk = new Chunk(x,y);
			map.put(coords, chunk);
		}
		return chunk;
	}
	
	@Override
	public String toString(){
		String ret = "";
		for(Chunk ch : map.values()){
			ret += ch + "\n";
		}
		return ret;
	}
	
	public int getChunkSize() {
		return CHUNK_SIZE;
	}
	
	/**
	 * Iterator for the Grid class. Iterates through the objects
	 * in each chunk. The remove() method is supported.
	 * 
	 * @author Emmett
	 *
	 */
	private class GridIterator implements Iterator<T> {

		boolean hasNextChunk;
		boolean hasNextObject;
		Chunk currentChunk;
		Chunk nextChunk;
		T currentObject;
		
		Iterator<Chunk> iChunk;
		Iterator<T> iObj;
		
		public GridIterator() {
			
			iChunk = map.values().iterator();
			
			// Find initial nonempty chunk.
			while(iChunk.hasNext()) {
				currentChunk = iChunk.next();
				if(!currentChunk.isEmpty()) 
					break;
			}
			// Return if the grid contains no objects in any of its chunks.
			if(currentChunk == null || currentChunk.isEmpty()) {
				return;
			}
			// Scout ahead for the next nonempty chunk.
			while(iChunk.hasNext()) {
				nextChunk = iChunk.next();
				if(!nextChunk.isEmpty()) {
					break;
				}
			}
			// Set hasNextChunk.
			hasNextChunk = !(nextChunk == null || nextChunk.isEmpty());
			
			// Initialize iObj
			iObj = currentChunk.iterator();
		}
		
		public boolean hasNext() {
			return hasNextChunk || (iObj != null && iObj.hasNext());
		}
		
		public T next() {
			// If there is a next object in this chunk, return it.
			if(iObj.hasNext()) {
				return iObj.next();
			}
			// Otherwise, set currentChunk = nextChunk and search for a new nextChunk.
			// But first, if we are out of chunks, just return null.
			if(!hasNextChunk) {
				return null;
			}
			currentChunk = nextChunk;
			// Search for next nonempty chunk.
			while(iChunk.hasNext()) {
				nextChunk = iChunk.next();
				if(!nextChunk.isEmpty()) {
					break;
				}
			}
			// Set hasNextChunk.
			hasNextChunk = !(nextChunk == null || nextChunk == currentChunk || nextChunk.isEmpty());
			
			// Set new iObj iterator.
			iObj = currentChunk.iterator();
			return iObj.next();
		}
		
		public void remove() {
			iObj.remove();
		}
		
	}
	
	public class Chunk extends HashSet<T> {
		
		private static final long serialVersionUID = -1953294672491456887L;

//		public static final int SIZE = 20; 
		
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
			coords[0] = x*CHUNK_SIZE;
			coords[1] = y*CHUNK_SIZE;
			return coords;
		}
		
		public int getGridX() {
			return x;
		}
		
		public int getGridY() {
			return y;
		}
		
		public int getWorldX() {
			return x*CHUNK_SIZE;
		}
		
		public int getWorldY() {
			return y*CHUNK_SIZE;
		}
		
		@Override
		public boolean equals(Object o) {
			
			if(!(o instanceof Grid.Chunk)) {
				return false;
			}
			
			return (x == ((Grid.Chunk)o).x) && (y == ((Grid.Chunk)o).y);
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
	
	// TESTING
	public static void main(String[] args) {
		
		// Initialize grid and put in some chunks to start.
		System.out.print("Initializing...");
		Grid<Entity> grid = new Grid<Entity>(20);
		for(int i = -5; i <= 5; i++) {
			for(int j = -5; j <= 5; j++) {
				grid.add(new Entity(Math.signum(i)*i*i*grid.getChunkSize()/10, Math.signum(j)*j*j*grid.getChunkSize()/10, null));
			}
		}
//		grid.add(new Entity(grid.getChunkSize(), 0, null));
		System.out.println("done.");
		
		// Print out number of chunks.
		System.out.println("Number of chunks = " + grid.map.size());
		
		// Get nearby test. 
//		LinkedList<Entity> nearby = grid.getInDiskMut(0,0.1,2*Chunk.SIZE + 1);
//		System.out.println("Number of nearby entities = " + nearby.size());
//		for(Entity o : nearby) {
//			System.out.println("Coords: x = " + o.getX()/Chunk.SIZE + "   y = " + o.getY()/Chunk.SIZE);
//		}
		
		for(Iterator<Entity> it = grid.iterator(); it.hasNext(); ) {
			Entity e = it.next();
			if(e.getX() == 0 && e.getY() == 0) {
				it.remove();
			}
		}
		
		// Iterator test.
		System.out.println("Entities:");
		int counter = 0;
		for(Entity e : grid) {
			counter++;
			System.out.println("Entity #" + counter + ": x = " + e.getX()/grid.getChunkSize() + "  y = " + e.getY()/grid.getChunkSize());
		}
	}
	
}
