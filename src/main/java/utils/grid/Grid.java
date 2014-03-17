package utils.grid;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

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
	
	private HashMap<HashCoords,Chunk> map;
//	private HashMap<AbstractOrganism,Chunk> orgos;
	private HashCoords coords;
	
	public Grid() {
		map = new HashMap<HashCoords,Chunk>();
		coords = new HashCoords();
		
//		orgos = new HashMap<AbstractOrganism,Chunk>();
	}
	
	/**
	 * Loop through the HashMap and remove any mappings which
	 * map to null or an empty Chunk.
	 */
	public void removeEmpties() {
		for(Iterator<Chunk> i = map.values().iterator(); i.hasNext(); ) {
			Chunk chunk = i.next();
			if( chunk == null || chunk.isEmpty() ) {
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
	public void add(AbstractOrganism orgo) {
		
		// Compute the appropriate chunk coordinates of the AbstractOrganism.
		int x = (int)(orgo.getX()/Chunk.SIZE);
		int y = (int)(orgo.getY()/Chunk.SIZE);
		coords.set(x,y);
		
		// Add organism to chunk via getChunk method.
		Chunk c = summonChunk(x,y);
		c.add(orgo);
		// Add organism to orgos map.
//		orgos.put(orgo,c);
	}
	
	/**
	 * Returns the value at coordinates (x,y).
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
//		Chunk c = map.get(coords);
//		if(c != null) {
//			Set<AbstractOrganism> keySet = orgos.keySet();
//			for(AbstractOrganism o : c) {
//				keySet.remove(o);
//			}
//		}
		map.remove(coords);
	}
	
	/**
	 * Clears all Chunks and Organisms from the Grid.
	 */
	public void clear() {
		map.clear();
//		orgos.clear();
	}

	/**
	 * Returned iterator iterates through the chunks in the grid.
	 * The remove() method is supported.
	 */
	public Iterator<Chunk> iterator() {
		return map.values().iterator();
	}
	
//	/**
//	 * Duplicate of iterator() method; returns
//	 * the ChunkIterator.
//	 * @return
//	 */
//	public Iterator<Chunk> chunkIterator() {
//		return new ChunkIterator();
//	}
	
//	/**
//	 * Returns an iterator which iterates over all AbstractOrganisms
//	 * recorded in the Grid.
//	 * 
//	 * @return
//	 */
//	public Iterator<AbstractOrganism> organismIterator() {
//		return new OrganismIterator();
//	}
	
	/**
	 * Returns a list of all values within a radius r of x and y, in
	 * Grid coordinates.
	 * 
	 * TODO: Check so you don't go over boundaries, or add in buffer
	 * in the Environment class.
	 * 
	 * TODO: Needs to be tested with a visualizer to make sure it works
	 * correctly.
	 * 
	 * @param x
	 * @param y
	 * @param r
	 * @return
	 */
	public HashSet<Chunk> getAllWithin(double x_0, double y_0, double r) {
		
		HashSet<Chunk> values = new HashSet<Chunk>();
		
		for(int y = (int)Math.ceil(y_0); y - y_0 < r; y++) {
			double r_x = Math.sqrt( r*r - (y - y_0)*(y - y_0) );
			for(int x = (int)(x_0 - r_x); x < x_0 + r_x; x++) {
				coords.set(x, y);
				Chunk chunk = map.get(coords);
				if(chunk != null && !chunk.isEmpty()) {
					values.add(chunk);
				}
			}
		}
		for(int y = (int)Math.ceil(y_0) - 2; y_0 - y - 1 < r; y-- ) {
			double r_x = Math.sqrt( r*r - (y - y_0 + 1)*(y - y_0 + 1) );
			for(int x = (int)(x_0 - r_x); x < x_0 + r_x; x++) {
				Chunk chunk = map.get(coords);
				if(chunk != null && !chunk.isEmpty()) {
					values.add(chunk);
				}
			}
		}
		for(int x = (int)(x_0 - r); x + x_0 < r; x++) {
			coords.set(x, (int)Math.ceil(y_0) - 1);
			Chunk chunk = map.get(coords);
			if(chunk != null && !chunk.isEmpty()) {
				values.add(chunk);
			}
		}
		
		return values;
	}
	
	/**
	 * Returns the chunks within the bounding box determined by the
	 * points (x_1,y_1) and (x_2,y_2).
	 * 
	 * @param x_1
	 * @param y_1
	 * @param x_2
	 * @param y_2
	 * @return chunks
	 */
	public HashSet<Chunk> getAllInBoundingBox(double x_1, double y_1, double x_2, double y_2) {
		
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
				if(chunk != null && !chunk.isEmpty()) {
					chunks.add(chunk);
				}
				
			}
		}
		
		return chunks;
	}
	
	/**
	 * As getAllWithin, but returns only the Chunks which lie not left of
	 * not under the original position.
	 * 
	 * @param x_0
	 * @param y_0
	 * @param r
	 * @return
	 */
	public HashSet<Chunk> getAllWithinAsym(double x_0, double y_0, double r) {
		
		HashSet<Chunk> values = new HashSet<Chunk>();
		
		for(int y = (int)Math.ceil(y_0); y - y_0 < r; y++) {
			double r_x = Math.sqrt( r*r - (y - y_0)*(y - y_0) );
			for(int x = (int)(x_0); x < x_0 + r_x; x++) {
				coords.set(x, y);
				Chunk chunk = map.get(coords);
				if(chunk != null && !chunk.isEmpty()) {
					values.add(chunk);
				}
			}
		}
		for(int x = (int)(x_0); x + x_0 < r; x++) {
			coords.set(x, (int)Math.ceil(y_0) - 1);
			Chunk chunk = map.get(coords);
			if(chunk != null && !chunk.isEmpty()) {
				values.add(chunk);
			}
		}
		
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
				if(!(o_x == c.getGridX() & o_y == c.getGridY())) {
					i.remove();
					toAdd.add(o);
				}
			}
		}
		
		for(AbstractOrganism o : toAdd) {
			this.add(o);
		}
	}
	
//	public void updateChunks() {
//		for(AbstractOrganism o : orgos.keySet()) {
//			coords.set((int)(o.getX()/Chunk.SIZE), (int)(o.getY()/Chunk.SIZE));
//			Chunk actualChunk = map.get(coords);
//			Chunk mapChunk = orgos.get(o);
//			
//			if(mapChunk != actualChunk) { // Possible bug: check for nulls?
//				mapChunk.remove(o);
//				actualChunk.add(o);
//				orgos.put(o,actualChunk);
//			}
//		}
//	}
	
	/**
	 * Returns the chunk at coordinates (x,y).
	 * Creates the chunk if none exists.
	 * 
	 * @param x
	 * @param y
	 * @return chunk
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
	
//	private class ChunkIterator implements Iterator<Chunk> {
//
//		private Iterator<Chunk> i;
//		private boolean iHasNext;
//		private Chunk iCurrent;
//		
//		public ChunkIterator() {
//			i = map.values().iterator();
//			iHasNext = i.hasNext();
//		}
//		
//		public boolean hasNext() {
//			return iHasNext;
//		}
//
//		public Chunk next() {
//			iCurrent = i.next();
//			iHasNext = i.hasNext();
//			return iCurrent;
//		}
//
//		public void remove() {
//			Set<AbstractOrganism> keySet = orgos.keySet();
//			for(AbstractOrganism o : iCurrent) {
//				keySet.remove(o);
//			}
//			i.remove();
//		}
//		
//	}
//	
//	private class OrganismIterator implements Iterator<AbstractOrganism> {
//		
//		private Iterator<AbstractOrganism> i;
//		private boolean hasNext;
//		private AbstractOrganism current;
//		
//		public OrganismIterator(){
//			i = orgos.keySet().iterator();
//			hasNext = i.hasNext();
//		}
//		
//		public boolean hasNext() {
//			return hasNext;
//		}
//
//		public AbstractOrganism next() {
//			current = i.next();
//			hasNext = i.hasNext();
//			return current;
//		}
//
//		public void remove() {
//			Chunk c = orgos.get(current);
//			c.remove(current);
//			i.remove();
//		}
//	}
}
