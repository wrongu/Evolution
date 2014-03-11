package utils.grid;

public class HashCoords {
	
	public static final int MAX = 65536;
	public static final int HALF_MAX = 32768;
	
	private int x;
	private int y;

	public HashCoords() {
		x = 0;
		y = 0;
	}
	
	public int hashCode() {
		return (shorten(x) << 16) | shorten(y);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int[] toCoords() {
		return new int[] {x,y};
	}
	
	public void set(int x, int y) {
		if(x < -HALF_MAX | x >= HALF_MAX | y < -HALF_MAX | y >= HALF_MAX) {
			throw new HashCoordsOutOfBoundsException();
		}
		this.x = x;
		this.y = y;
	}
	
	private int shorten(int a){
		int sgn = Integer.MIN_VALUE;
		int val = Short.MAX_VALUE;
		sgn &= a;
		sgn = sgn >>> 16;
		val &= a;
		return sgn | val;
	}
	
	public String toString() {
		return "(" + Integer.toString(x) + ", " + Integer.toString(y) + ")";
	}
	
	public void print() {
//		System.out.println("SHORT_LENGTH - 1 = " + Integer.toBinaryString(SHORT_LENGTH -1));
		System.out.println("x = " + Integer.toBinaryString(x));
		System.out.println("y = " + Integer.toBinaryString(y));
		System.out.println("hashCode() = " + Integer.toBinaryString(hashCode()));
	}
	
	private class HashCoordsOutOfBoundsException extends RuntimeException {
	}
}
