package utils.grid;

import java.util.HashMap;
// TODO refactor into maven tests directory
public class GridTest {
	
	public static void main(String[] args) {
		Grid<Integer> grid = new Grid<Integer>();
		
		grid.put(0, 0, 1);
		grid.put(2, 0, 2);
		grid.put(4, 1, 3);
		grid.put(0, -1, 4);
		grid.put(-257, 1000, 5);
		grid.put(1, 1, 6);
		
		System.out.println(grid.get(0,0));
		System.out.println(grid.get(0,1));
		System.out.println(grid.get(4,1));
		System.out.println();
		
		grid.remove(0, 0);
		
		System.out.println(grid.get(0,0));
		System.out.println(grid.get(0,1));
		System.out.println(grid.get(4,1));
		System.out.println();
		
		for(Integer i : grid) {
			System.out.print(i + " ");
		}
		System.out.println();
	}
	
}
