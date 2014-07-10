package utils;

import java.util.Random;

import applet.Config;

public class Util {
	
	private static int seed = Config.instance.getInt("SEED");
	public static Random random = new Random(seed);
	
	public static int[] randomOrder(int n) {
		if(n < 0) {
			return null;
		}
		
		int[] order = new int[n];
		for(int i = 0; i < n; i++) {
			order[i] = i;
		}
		
		for(int i = 0; i < n; i++) {
			// Swap order[i] with order[j] for a randomly determined j.
			int j = random.nextInt(n);
			int holder = order[i];
			order[i] = order[j];
			order[j] = holder;
		}
		
		return order;
	}
}
