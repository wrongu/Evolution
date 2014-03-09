package environment.generators;

import java.util.Random;

import graphics.RandomGeneratorVisualizer;

public class PerlinGenerator implements IGenerator {

	/** the seed used for random number generation */
	private long seed;
	/** layers of generation, each half resolution of the last */
	private int octaves;
	/** how large 1 unit of noise is in world-coordinates (i.e. size of grid) */
	private double scale;
	
	/**
	 * 
	 * @param octaves layers of generation, each half resolution of the last
	 * @param scale how large 1 unit of noise is in world-coordinates (i.e. size of grid)
	 * @param s the seed used for random number generation
	 */
	public PerlinGenerator(int octaves, double scale, long s){
		this.octaves = octaves;
		this.scale = scale;
		this.seed = s;
	}
	
	public void setSeed(long s) {
		this.seed = s;
	}

	public double terrainValue(double x, double y) {
		double value = 0;
		double max = 0.;
		for(int o=0; o < this.octaves; o++){
			// the width of a single cell of this octave is proportional to 2^o (0th octave is finest-detail)
			int octave_factor = 1 << o; // 2^o
			// the amplitude of this octave is inversely proportaional to the size
			// (this makes low-frequency noise stronger and high-frequency less strong)
			double amplitude = 1. / ((double) (1 << (this.octaves - o - 1)));
			max += amplitude;
			// convert coordinates to the worldspace of the RNG
			double cell_width = this.scale * octave_factor;
			// get nearest (floor) x and y grid positions.
			// note that the modular division makes the topology toroidal (repeats in both x and y)
			long xbin = ((long) Math.floor(x / cell_width));
			long ybin = ((long) Math.floor(y / cell_width));
			// xoff and yoff are in [0,1), and are the coordinates within the grid cell of the point (x,y)
			double xoff = x / cell_width - xbin;
			double yoff = y / cell_width - ybin;
			// get a random gradient direction at each of the 4 surrounding corners
			// (Note that using salted_rng means that the value is always the same for a given (seed,o,x,y) tuple
			double r00 = 2 * Math.PI * salted_rng(this.seed, o, xbin, ybin);
			double r01 = 2 * Math.PI * salted_rng(this.seed, o, xbin, ybin+1);
			double r10 = 2 * Math.PI * salted_rng(this.seed, o, xbin+1, ybin);
			double r11 = 2 * Math.PI * salted_rng(this.seed, o, xbin+1, ybin+1);
			// contribution of each gradient computed as the dot of (x,y) with the gradient direction
			double c00 = Math.cos(r00) * xoff + Math.sin(r00) * yoff;
			double c01 = Math.cos(r01) * xoff + Math.sin(r01) * (1. - yoff);
			double c10 = Math.cos(r10) * (1. - xoff) + Math.sin(r10) * yoff;
			double c11 = Math.cos(r11) * (1. - xoff) + Math.sin(r11) * (1. - yoff);
			// values are combined with smoothing function 3x^2-2x^3
			double Sx = xoff*(3*xoff - 2*xoff*xoff);
			double Sy = yoff*(3*yoff - 2*yoff*yoff);
			double top = c00 + Sx*(c10-c00); // interpolate top 2 xs
			double bottom = c01 + Sx*(c11-c01); // interpolate bottom 2 xs
			double interpolated = top + Sy*(bottom-top); // interpolate on y
			value += amplitude * interpolated; 
		}
		// map from [-max, max] to [0,1]
		return (value + max) / (2*max);
	}
	
	private static double salted_rng(long ... seeds){
		long salted = 0L;
		for(long s : seeds){
			salted <<= 8;
			salted ^= s;
		}
		return new Random(salted).nextDouble();
	}
	
	public static void main(String[] args){
		// TESTING / VISUALIZING
		PerlinGenerator gen = new PerlinGenerator(1, 10., 0L);
		RandomGeneratorVisualizer.display(gen, 300);
	}

}
