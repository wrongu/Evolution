package neuralevo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;


public class Grid implements Iterable<Square>{
	
	private HashMap<Integer,HashMap<Integer,Square>> map;
	
	public Grid() {
		map = new HashMap<Integer,HashMap<Integer,Square>>();
	}
	
	public void add(NOrganism o) {
		
		int colNum = (int)(o.getX()/Square.SIZE);
		int rowNum = (int)(o.getY()/Square.SIZE);
		
		HashMap<Integer,Square> col = map.get(colNum);
		Square square;
		
		// Check to see if column exists. If not, then create it.
		if(col == null) {
			col = new HashMap<Integer,Square>();
			map.put(colNum, col);
		}

		// Check to see if square exists in column. If not, then create it.
		square = col.get(rowNum);
		if(square == null) {
			square = new Square(colNum, rowNum);
			col.put(rowNum,square);
		}
		
		// Put organism in the square.
		square.add(o);
	}
	
	public Square get(int x, int y) {
		HashMap<Integer,Square> col = map.get(x);
		if(col == null)
			return null;
		return col.get(y);
	}
	
	// Removes empty squares and columns. Should be called every few seconds.
	public void removeEmpties() {
		for(Iterator<Square> i = iterator(); i.hasNext(); ) {
			Square s = i.next();
			if(s.isEmpty())
				i.remove();
		}
		for(Iterator<HashMap<Integer,Square>> col = map.values().iterator(); col.hasNext(); ){
			HashMap<Integer,Square> c = col.next();
			if(c.isEmpty()) {
				col.remove();
			}
		}
	}
	
	public Iterator<Square> iterator() {
		return new GridIterator();
	}
	
	// Debugging.
	public void print() {
		int count = 0;
		for(Square s : this) {
			count++;
		}
		System.out.println("# of Squares: " + count);
		count = 0;
		for(HashMap<Integer,Square> col : map.values()) {
			count++;
		}
		System.out.println("# of Columns: " + count);
		
		count = 0;
		for(Square s : this) {
			count += s.size();
		}
		System.out.println("# of Organisms: " + count);
	}
	
	// Iterator class for Grid.
	private class GridIterator implements Iterator<Square> {
//		return map.values().iterator();
		
		private Iterator<HashMap<Integer,Square>> colIterator = map.values().iterator();
		private Iterator<Square> rowIterator;
		private int nonemptyColumns;
		private int currentNonemptyColumn;
		private boolean freshColumn;
		private Square currentSquare;
		private int lastRow;
		private int lastCol;

		public GridIterator() {
			// Determine number of nonempty columns.
			freshColumn = true;
			nonemptyColumns = 0;
			for(HashMap<Integer,Square> col : map.values()) {
				nonemptyColumns += col.isEmpty() ? 0 : 1;
			}
			currentNonemptyColumn = 0;
			
			// Iterate to the first element of the first nonempty column.
			nextNonemptyColumn();
		}
		
		public boolean hasNext() {
			return (currentNonemptyColumn < nonemptyColumns) || (rowIterator != null && rowIterator.hasNext());
		}
		
		public Square next() {

			if(currentSquare != null) {
				lastCol = currentSquare.getX();
				lastRow = currentSquare.getY();
			}

			// If there are no elements left in the current column, get the next column.
			if(!rowIterator.hasNext()) {
				nextNonemptyColumn();
			}
			
			freshColumn = false;
			return currentSquare = rowIterator.next();
		}
		
		public void remove() {
			if(freshColumn) {
				(map.get(lastCol)).remove(lastRow);
			} else {
				rowIterator.remove();
			}
		}
		
		private void nextNonemptyColumn() {
			currentNonemptyColumn++;
			freshColumn = true;
			HashMap<Integer,Square> col;
			// Loop through the columns.
			while(colIterator.hasNext()){
				col = colIterator.next();
				// Stop looping and return if the iterator has found a nonempty column.
				if(!col.isEmpty()) {
					rowIterator = col.values().iterator();
					break;
				}
			}
		}
	}

}