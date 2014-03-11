package utils.grid;

import java.util.HashMap;
import java.util.Iterator;

public class Grid<T> implements Iterable<T> {
	
	private HashMap<HashCoords,T> map;
	private HashCoords coords;
	
	public Grid() {
		map = new HashMap<HashCoords,T>();
		coords = new HashCoords();
	}
	
	public void removeNulls() {
		for(Iterator<T> i = map.values().iterator(); i.hasNext(); ) {
			T t = i.next();
			if(t == null) {
				i.remove();
			}
		}
	}
	
	public void put(int x, int y, T object) {
		coords.set(x,y);
		map.put(coords, object);
	}
	
	public T get(int x, int y) {
		coords.set(x,y);
		return map.get(coords);
	}
	
	public void remove(int x, int y) {
		coords.set(x, y);
		map.remove(coords);
	}

	public Iterator<T> iterator() {
		return map.values().iterator();
	}
}
