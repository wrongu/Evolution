package utils.grid;

import java.util.HashMap;
import java.util.Iterator;

/**
 * A wrapper for a HashMap<HashCoords,T> object. Contains
 * put and get methods for accessing the HashMap, only the
 * key is always a pair of integers, and the values are of
 * class T.
 * 
 * The iterator() method returns the iterator for the
 * collection view of the values of the HashMap, so it is
 * possible to easily loop through the values stored in the
 * Grid without knowing the coordinates.
 * 
 * @author Emmett
 *
 * @param <T>
 */
public class Grid<T> implements Iterable<T> {
	
	private HashMap<HashCoords,T> map;
	private HashCoords coords;
	
	public Grid() {
		map = new HashMap<HashCoords,T>();
		coords = new HashCoords();
	}
	
	/**
	 * Loop through the HashMap and remove any mappings which
	 * map to null.
	 */
	public void removeNulls() {
		for(Iterator<T> i = map.values().iterator(); i.hasNext(); ) {
			T t = i.next();
			if(t == null) {
				i.remove();
			}
		}
	}
	
	/**
	 * Map the coordinates (x,y) to value.
	 * 
	 * @param x
	 * @param y
	 * @param value
	 */
	public void put(int x, int y, T value) {
		coords.set(x,y);
		map.put(coords, value);
	}
	
	/**
	 * Returns the value at coordinates (x,y).
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public T get(int x, int y) {
		coords.set(x,y);
		return map.get(coords);
	}
	
	/**
	 * Removes the mapping at coordinates (x,y). Though,
	 * removal of mappings should be done by the iterator
	 * due to concurrency issues. 
	 * 
	 * @param x
	 * @param y
	 */
	public void remove(int x, int y) {
		coords.set(x, y);
		map.remove(coords);
	}

	/**
	 * Returns the iterator for the the values of the HashMap.
	 */
	public Iterator<T> iterator() {
		return map.values().iterator();
	}
}
