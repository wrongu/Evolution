package utils.grid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import bio.organisms.AbstractOrganism;

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
public class Grid implements Iterable<Chunk> {
	
	private static final double ROOT_2 = Math.sqrt(2);
	
	private HashMap<HashCoords,Chunk> map;
	private HashCoords coords;
	
	public Grid() {
		map = new HashMap<HashCoords,Chunk>();
		coords = new HashCoords();
	}
	
	/**
	 * Loop through the HashMap and remove any mappings which
	 * map to null or an empty Chunk.
	 */
	public void removeEmpties() {
		LinkedList<Chunk> to_remove = new LinkedList<Chunk>();
		for(Iterator<Chunk> i = map.values().iterator(); i.hasNext(); ) {
			Chunk chunk = i.next();
			if( chunk == null) i.remove();
			else if(chunk.isEmpty()) to_remove.add(chunk);
		}
		for(Chunk rem : to_remove) removeChunk(rem.getGridX(), rem.getGridY());
	}
	
	/**
	 * Adds a new AbstractOrganism to the appropriate chunk.
	 * Creates a chunk if such a chunk does not exist.
	 * 
	 * @param orgo
	 */
	public void add(AbstractOrganism orgo) {
		
		// Compute the appropriate chunk coordinates of the AbstractOrganism.
		int x = (int)(orgo.getX()/Chunk.SIZE);
		int y = (int)(orgo.getY()/Chunk.SIZE);
		coords.set(x,y);
		
		// Add organism to chunk.
		Chunk c = summonChunk(x,y);
		c.add(orgo);
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
	 * Removes the mapping at coordinates (x,y). Though,
	 * removal of mappings should be done by the iterator
	 * whenever possible due to concurrency issues. 
	 * 
	 * @param x
	 * @param y
	 */
	public void removeChunk(int x, int y) {
		coords.set(x, y);
		map.remove(coords);
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
	 */
	public Iterator<Chunk> iterator() {
		return map.values().iterator();
	}
	
	/**
	 * Returns a list of all values within a radius r of x and y, in
	 * Grid coordinates.
	 * 
	 * TODO: Check so you don't go over boundaries, or add in buffer
	 * in the Environment class.
	 * 
	 * @param x
	 * @param y
	 * @param r
	 * @return
	 */
	public HashSet<Chunk> getAllWithin(double x_0, double y_0, double r) {
				
		HashSet<Chunk> values = new HashSet<Chunk>();
		
		r += ROOT_2;
		int x_L = (int)Math.floor(x_0 - r);
		int x_U = (int)Math.ceil(x_0 + r);
		int y_L = (int)Math.floor(y_0 - r);
		int y_U = (int)Math.ceil(y_0 + r);
		int x_range = x_U - x_L + 1;
		int y_range = y_U - y_L + 1;
		boolean[][] cornerMarkers = new boolean[x_range][y_range];
		
		for(int i = 0; i < x_range; i++) {
			for(int j = 0; j < y_range; j++) {
				double dx = (i + x_L) - x_0;
				double dy = (j + y_L) - y_0;
				cornerMarkers[i][j] = dx*dx + dy*dy < r*r;
			}
		}
		
		for(int i = 0; i < x_range - 1; i++) {
			for(int j = 0; j < y_range - 1; j++) {
				if(cornerMarkers[i][j] & cornerMarkers[i+1][j] & cornerMarkers[i][j+1] & cornerMarkers[i+1][j+1]) {
					coords.set(i + x_L, j + y_L);
					values.add(map.get(coords));
				}
			}
		}
		
		values.remove(null);
		
		return values;
	}
	
	/**
	 * Get all Chunks which are potentially within a distance r
	 * of the given Chunk c.
	 * 
	 * @param c
	 * @param r
	 * @return
	 */
	public HashSet<Chunk> getAllWithin(Chunk c, double r) {
		HashSet<Chunk> values = new HashSet<Chunk>();
		
		r += ROOT_2;
		int x_0 = c.getGridX();
		int y_0 = c.getGridY();
		int r_ceil = (int)Math.ceil(r);
		int x_L = x_0 - r_ceil;
		int x_U = x_0 + r_ceil + 1;
		int y_L = y_0 - r_ceil;
		int y_U = y_0 + r_ceil + 1;
		int x_range = x_U - x_L + 1;
		int y_range = y_U - y_L + 1;
		boolean[][] cornerMarkers = new boolean[x_range][y_range];
		
		for(int i = 0; i <= r_ceil; i++) {
			for(int j = 0; j <= r_ceil; j++) {
				double dx = (i + x_L) - x_0;
				double dy = (j + y_L) - y_0;
				cornerMarkers[i][j] = dx*dx + dy*dy < r*r;
			}
		}
		for(int i = 0; i <= r_ceil; i++) {
			for(int j = r_ceil + 1; j < y_range; j++) {
				double dx = (i + x_L) - x_0;
				double dy = (j + y_L) - (y_0 + 1);
				cornerMarkers[i][j] = dx*dx + dy*dy < r*r;
			}
		}
		for(int i = r_ceil + 1; i < x_range; i++) {
			for(int j = 0; j <= r_ceil; j++) {
				double dx = (i + x_L) - (x_0 + 1);
				double dy = (j + y_L) - y_0;
				cornerMarkers[i][j] = dx*dx + dy*dy < r*r;
			}
		}
		for(int i = r_ceil + 1; i < x_range; i++) {
			for(int j = r_ceil + 1; j < y_range; j++) {
				double dx = (i + x_L) - (x_0 + 1);
				double dy = (j + y_L) - (y_0 + 1);
				cornerMarkers[i][j] = dx*dx + dy*dy < r*r;
			}
		}
		
		for(int i = 0; i < x_range - 1; i++) {
			for(int j = 0; j < y_range - 1; j++) {
				if(cornerMarkers[i][j] & cornerMarkers[i+1][j] & cornerMarkers[i][j+1] & cornerMarkers[i+1][j+1]) {
					coords.set(i + x_L, j + y_L);
					values.add(map.get(coords));
				}
			}
		}
		
		values.remove(null);
		
		return values;
	}
	
	/**
	 * Returns the chunks intersecting the bounding box [x_1,x_2] x [y_1,y_2],
	 * in interval notation.
	 * 
	 * @param x_1
	 * @param x_2
	 * @param y_1
	 * @param y_2
	 * @return chunks
	 */
	public HashSet<Chunk> getAllInBoundingBox(double x_1, double x_2, double y_1, double y_2) {
		
		HashSet<Chunk> chunks = new HashSet<Chunk>();
		
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
		
		for(int x = (int)x_1; x < x_2; x++) {
			for(int y = (int)y_1; y < y_2; y++) {
				
				coords.set(x, y);
				Chunk chunk = map.get(coords);
				if(chunk != null) { // && !chunk.isEmpty()
					chunks.add(chunk);
				}
				
			}
		}
		
		chunks.remove(null);
		
		return chunks;
	}
	
	/**
	 * As getAllWithin, but returns only the Chunks which lie not left of and
	 * not under the original position. To be used with mutual interactions
	 * between entities to avoid duplicate effects.
	 * 
	 * @param x_0
	 * @param y_0
	 * @param r
	 * @return
	 */
	public HashSet<Chunk> getAllWithinAsym(double x_0, double y_0, double r) {
		
		HashSet<Chunk> values = new HashSet<Chunk>();
		r += ROOT_2;
		int x_L = (int)Math.floor(x_0);
		int x_U = (int)Math.ceil(x_0 + r);
		int y_L = (int)Math.floor(y_0);
		int y_U = (int)Math.ceil(y_0 + r);
		int x_range = x_U - x_L + 1;
		int y_range = y_U - y_L + 1;
		boolean[][] cornerMarkers = new boolean[x_range][y_range];
		
		for(int i = 0; i < x_range; i++) {
			for(int j = 0; j < y_range; j++) {
				double dx = (i + x_L) - x_0;
				double dy = (j + y_L) - y_0;
				cornerMarkers[i][j] = dx*dx + dy*dy < r*r;
			}
		}
		
		for(int i = 0; i < x_range - 1; i++) {
			for(int j = 0; j < y_range - 1; j++) {
				if(cornerMarkers[i][j] & cornerMarkers[i+1][j] & cornerMarkers[i][j+1] & cornerMarkers[i+1][j+1]) {
					coords.set(i + x_L, j + y_L);
					values.add(map.get(coords));
				}
			}
		}
		
		values.remove(null);
		
		return values;
		
	}
	
	/**
	 * Get all Chunks which are potentially within a distance r
	 * of the given Chunk c, but lie not below and not to the
	 * left of c. To be used for mutual interactions to avoid
	 * duplicate effects.
	 * 
	 * @param c
	 * @param r
	 * @return
	 */
	public HashSet<Chunk> getAllWithinAsym(Chunk c, double r) {
		HashSet<Chunk> values = new HashSet<Chunk>();
		
		r += ROOT_2;
		int x_0 = c.getGridX();
		int y_0 = c.getGridY();
		int r_ceil = (int)Math.ceil(r);
		int x_L = x_0;
		int x_U = x_0 + r_ceil + 1;
		int y_L = y_0;
		int y_U = y_0 + r_ceil + 1;
		int x_range = x_U - x_L + 1;
		int y_range = y_U - y_L + 1;
		boolean[][] cornerMarkers = new boolean[x_range][y_range];
		
		cornerMarkers[0][0] = true;
		for(int i = 1; i < x_range; i++) {
			cornerMarkers[i][0] = true;
		}
		for(int j = 1; j < y_range; j++) {
			cornerMarkers[0][j] = true;
		}
		for(int i = 1; i < x_range; i++) {
			for(int j = 1; j < y_range; j++) {
				double dx = (i + x_L) - (x_0 + 1);
				double dy = (j + y_L) - (y_0 + 1);
				cornerMarkers[i][j] = dx*dx + dy*dy < r*r;
			}
		}
		
		for(int i = 0; i < x_range - 1; i++) {
			for(int j = 0; j < y_range - 1; j++) {
				if(cornerMarkers[i][j] & cornerMarkers[i+1][j] & cornerMarkers[i][j+1] & cornerMarkers[i+1][j+1]) {
					coords.set(i + x_L, j + y_L);
					values.add(map.get(coords));
				}
			}
		}
		
		values.remove(null);
		
		return values;
	}
	
	/**
	 * Checks which entities are in which chunks. Moves entities
	 * to different chunks if necessary.
	 */
	public void updateChunks() {
		LinkedList<AbstractOrganism> toAdd = new LinkedList<AbstractOrganism>();
		
		for(Chunk c : map.values()) {
			for(Iterator<AbstractOrganism> i = c.iterator(); i.hasNext(); ) {
				AbstractOrganism o = i.next();
				int o_x = (int)(o.getX()/Chunk.SIZE);
				int o_y = (int)(o.getY()/Chunk.SIZE);
				if(!(o_x == c.getGridX() && o_y == c.getGridY())) {
					toAdd.add(o);
					i.remove();
				}
			}
		}
		
		for(AbstractOrganism o : toAdd) {
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
		for(Chunk ch : this){
			ret += ch + "\n";
		}
		return ret;
	}
	
	// TESTING
	public static void main(String[] args) {
		
		// Initialize grid and put in some chunks to start.
		System.out.print("Initializing...");
		Grid grid = new Grid();
		for(int i = -20; i <= 20; i++) {
			for(int j = -20; j <= 20; j++) {
				grid.summonChunk(i,j);
			}
		}
		System.out.println("done.");
		
		// Print out number of chunks.
		System.out.println("Number of chunks = " + grid.map.size());
		
		// Get nearby test.
		HashSet<Chunk> nearbyChunks = grid.getAllWithinAsym(grid.get(0,0),0.5);
		System.out.println("Number of nearby chunks = " + nearbyChunks.size());
		for(Chunk c : nearbyChunks) {
			System.out.println("Coords: x = " + c.getGridX() + "   y = " + c.getGridY());
		}
	}
	
}
