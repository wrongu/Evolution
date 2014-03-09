package environment.generators;

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

	private double interp(double a, double b, double bias){
		// values are combined with smoothing function 3x^2-2x^3
		double smooth = bias*bias*(3 - 2*bias);
		return a + smooth * (b - a);
	}
	
	private double noise3d(int x, int y, int z, int seed)
	{
		// pseudo-random number generation guaranteed to be the same for a given
		// tuple of (x, y, z, seed)
		int n = (1619 * x + 31337 * y + 52591 * z
				+ 1013 * seed) & 0x7fffffff;
		n = (n>>13)^n;
		n = (n * (n*n*60493+19990303) + 1376312589) & 0x7fffffff;
		return 1.0 - (double)n/1073741824;
	}

	public double terrainValue(double x, double y) {
		double value = 0.;
		double max = 0.;
		for(int o=0; o < this.octaves; o++){
			// the width of a single cell of this octave is proportional to 2^o (but reversed so 0th octave is coarsest-detail)
			int octave_factor = 1 << (this.octaves - o - 1);
			// the amplitude of this octave is inversely proportaional to the size
			// (this makes low-frequency noise stronger and high-frequency less strong)
			double amplitude = 1. / ((double) octave_factor);
			max += amplitude;
			// convert coordinates to the worldspace of the RNG
			double cell_width = this.scale * octave_factor;
			// get nearest (floor) x and y grid positions.
			// note that the modular division makes the topology toroidal (repeats in both x and y)
			int xlo = ((int) Math.floor(x / cell_width));
			int ylo = ((int) Math.floor(y / cell_width));
			int xhi = xlo+1, yhi = ylo+1;
			// xoff and yoff are in [0,1), and are the coordinates within the grid cell of the point (x,y)
			double xoff = x / cell_width - (double) xlo;
			double yoff = y / cell_width - (double) ylo;
			// get random value for each corner (pseudo-random in that it is 
			// always the same for a given permutation of arguments)
			double c00 = noise3d(xlo, ylo, o, (int) this.seed);
			double c01 = noise3d(xlo, yhi, o, (int) this.seed);
			double c10 = noise3d(xhi, ylo, o, (int) this.seed);
			double c11 = noise3d(xhi, yhi, o, (int) this.seed);
			// smoothly interpolate
			double bottom = interp(c00, c10, xoff);
			double top = interp(c01, c11, xoff);
			double interpolated = interp(bottom, top, yoff);
			value += amplitude * interpolated;
		}
		// map from [-max, max] to [0,1]
		return (value + max) / (2*max);
	}

	public static void main(String[] args){
		// TESTING / VISUALIZING
		int oct = 20;
		PerlinGenerator gen = new PerlinGenerator(oct, 30., 0L);
		RandomGeneratorVisualizer.display(gen, 300, oct);
	}

}
