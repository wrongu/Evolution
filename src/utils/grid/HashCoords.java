package utils.grid;

/**
 * Wraps two integer coordinates taking values between -32768 and 32767.
 * The hashCode() method return the concatenation of the two coordinates
 * in binary. Objects of this class are intended to be used as keys in a
 * HashMap.
 * 
 * @author Emmett
 *
 */

public class HashCoords {
	
	/**
	 * Maximum allowable index.
	 */
	public static final int MAX_INDEX = 32767;
	/**
	 * Minimum allowable index.
	 */
	public static final int MIN_INDEX = -32768;
	
	private int x;
	private int y;

	public HashCoords() {
		x = 0;
		y = 0;
	}
	
	/**
	 * Returns the binary concatenation of the two indices. E.g. if
	 * x = 0b1111111111111111 and
	 * y = 0b0000000000000001, then this method returns
	 * 0b11111111111111110000000000000001.
	 */
	public int hashCode() {
		return (shorten(x) << 16) | shorten(y);
	}
	
	/**
	 * Returns the current horizontal index.
	 * 
	 * @return x
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Returns the current vertical index.
	 * 
	 * @return y
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Returns the current horizontal and vertical indices x and y,
	 * respectively.
	 * 
	 * @return {x,y}
	 */
	public int[] toCoords() {
		return new int[] {x,y};
	}
	
	/**
	 * Sets the horizontal and vertical coordinates of this object.
	 * Throws a RuntimeException if the coordinates are out of bounds.
	 * 
	 * @param x
	 * @param y
	 */
	public void set(int x, int y) {
		if(x < MIN_INDEX | x > MAX_INDEX | y < MIN_INDEX | y > MAX_INDEX) {
			throw new HashCoordsOutOfBoundsException("HashCoords = ( " + x + " , " + y + " ).");
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
		return "( " + Integer.toString(x) + " , " + Integer.toString(y) + " )";
	}
	
	// DEBUGGING
	public void print() {
//		System.out.println("SHORT_LENGTH - 1 = " + Integer.toBinaryString(SHORT_LENGTH -1));
		System.out.println("x = " + Integer.toBinaryString(x));
		System.out.println("y = " + Integer.toBinaryString(y));
		System.out.println("hashCode() = " + Integer.toBinaryString(hashCode()));
	}
	
	private class HashCoordsOutOfBoundsException extends RuntimeException {
		
		public HashCoordsOutOfBoundsException(String msg) {
			super(msg);
		}
	}
}
