package environment.generators;

import java.util.Random;

import applet.Config;

import graphics.RandomGeneratorVisualizer;

public class PerlinGenerator implements IGenerator {
	
	public static final int TABLE_SIZE = 256;

	/** a table with some power-of-two (usually 256) entries used as a sort random index lookup */
	private int[] random_table;
	/** layers of generation, each half resolution of the last */
	private int octaves;
	/** how large the largest grid unit is. The 2nd octave is half as large, 3rd is 1/4 as large, etc.. */
	private double scale;
	/** a function to be applied to the output */
	private Filter filter;

	/**
	 * 
	 * @param octaves layers of generation, each half resolution of the last
	 * @param scale how large the largest grid unit is. The 2nd octave is half as large, 3rd is 1/4 as large, etc..
	 * @param s the seed used for random number generation
	 */
	public PerlinGenerator(int octaves, double scale, long s){
		this(octaves, scale, s, null);
	}

	/**
	 * 
	 * @param octaves layers of generation, each half resolution of the last
	 * @param scale how large the largest grid unit is. The 2nd octave is half as large, 3rd is 1/4 as large, etc..
	 * @param s the seed used for random number generation
	 * @param filter a filter function applied to the output
	 */
	public PerlinGenerator(int octaves, double scale, long s, Filter f){
		this.octaves = octaves;
		this.scale = scale;
		this.filter = f;
		this.random_table = new int[TABLE_SIZE];
		this.shuffle_table(s);
	}
	
	private void shuffle_table(long seed){
		// set things in order
		for(int i=0; i<TABLE_SIZE; i++) random_table[i] = i;
		// scramble everything
		Random r = new Random(seed);
		for(int i=0; i<TABLE_SIZE; i++){
			int temp = random_table[i];
			int swap = r.nextInt(TABLE_SIZE);
			random_table[i] = random_table[swap];
			random_table[swap] = temp;
		}
	}
	
	public void setSeed(long s) {
		this.shuffle_table(s);
	}

	private static double interp(double a, double b, double bias){
		// values are combined with smoothing function 3x^2-2x^3
		double smooth = bias*bias*(3 - 2*bias);
		return a + smooth * (b - a);
	}
	
	/**
	 * this allows indices of the table to "wrap around" the edges.
	 * @param i any integer
	 * @return i mapped to [0,TABLE_SIZE) using wrap-around (modulus)
	 */
	private int table_modulo(int i){
		int ret = (i % TABLE_SIZE);
		if(ret < 0) ret += TABLE_SIZE;
		return ret;
	}
	
	/**
	 * get a pseudo-random number in [0,1) based on the lookup table, x, and y
	 * @return
	 */
	private double pseudo_random2d(int x, int y)
	{
		// pseudo-random-number in [0,1) using lookup in the table
		int i = table_modulo(x);
		i = table_modulo(random_table[i] + y);
		return (double) random_table[i] / (double) TABLE_SIZE;
	}

	public double terrainValue(double x, double y) {
		double value = 0.;
		double max = 2.0 - Math.pow(2.0, -(this.octaves-1));
		for(int o=0; o < this.octaves; o++){
			// the width of a single cell of this octave is proportional to 2^o (but reversed so 0th octave is coarsest-detail)
			double octave_factor = 1 << (this.octaves - o - 1);
			// the amplitude of this octave is inversely proportaional to the size
			// (this makes low-frequency noise stronger and high-frequency less strong)
			double amplitude = 1. / octave_factor;
			// convert coordinates to the worldspace of the RNG
			// (scale gets finer as octaves go up)
			double cell_width = this.scale * amplitude;
			// get nearest (floor) x and y grid positions on this octave.
			int xlo = ((int) Math.floor(x / cell_width));
			int ylo = ((int) Math.floor(y / cell_width));
			int xhi = xlo+1, yhi = ylo+1;
			// get random gradient direction for each corner (pseudo-random in that it is 
			// always the same for a given permutation of arguments)
			double d00 = 2. * Math.PI * pseudo_random2d(xlo, ylo);
			double d01 = 2. * Math.PI * pseudo_random2d(xlo, yhi);
			double d10 = 2. * Math.PI * pseudo_random2d(xhi, ylo);
			double d11 = 2. * Math.PI * pseudo_random2d(xhi, yhi);
			// xoff and yoff are in [0,1), and are the coordinates within the grid cell of the point (x,y)
			double xoff = x / cell_width - (double) xlo;
			double yoff = y / cell_width - (double) ylo;
			// compute the effect of each corner's gradient on (x,y)
			double c00 = Math.cos(d00) * xoff + Math.sin(d00) * yoff;
			double c01 = Math.cos(d01) * xoff + Math.sin(d01) * (yoff-1.);
			double c10 = Math.cos(d10) * (xoff-1.) + Math.sin(d10) * yoff;
			double c11 = Math.cos(d11) * (xoff-1.) + Math.sin(d11) * (yoff-1.);
			// smoothly interpolate
			double bottom = interp(c00, c10, xoff);
			double top = interp(c01, c11, xoff);
			double interpolated = interp(bottom, top, yoff);
			value += amplitude * interpolated;
		}
		// map from [-max, max] to [0,1]
		double zero_to_one = (value + max) / (2*max);
		if(this.filter != null)
			return this.filter.applyFilter(zero_to_one, x, y);
		else
			return zero_to_one;
	}
	
	public int[] getTable(){
		return this.random_table;
	}
	
	public float[] getTableNormalized(){
		float[] normalized = new float[TABLE_SIZE];
		for(int i=0; i<random_table.length; i++)
			normalized[i] = (float) random_table[i] / (float) TABLE_SIZE;
		return normalized;
	}
	
	public double getScale(){
		return scale;
	}

	public int getOctaves() {
		return octaves;
	}
	
	public static abstract class Filter{
		/** given a value in [0, 1], return a value between [0,1] (presumably with some transformation applied) */
		public abstract double applyFilter(double val, double x, double y);
	}

	public static void main(String[] args){
		// TESTING / VISUALIZING
		int oct = 1;
		PerlinGenerator gen = new PerlinGenerator(oct, 60., Config.instance.getLong("SEED"), new Filter(){
			@Override
			public double applyFilter(double val, double x, double y) {
				return Math.floor(val * 16.0) / 16.0;
			}
		});
		RandomGeneratorVisualizer.display(gen, 300, oct);
	}
}
