package neuralevo;

import org.jblas.DoubleMatrix;

public class NBrain {
	
	// Output indices
	public static final int OUTPUTS = 5;
	public static final int THRUST_OUT = 0;
	public static final int TURN_OUT = 1;
	public static final int ATTACK = 2;
	public static final int MATE = 3;
	public static final int TALK = 4;
	
	// Inputs indices
	public static final int INPUTS = 5;
	public static final int THRUST_IN = 0;
	public static final int TURN_IN = 1;
	public static final int ENERGY = 2;
	public static final int LISTEN = 3;
	public static final int TOUCH = 4;
	
	// Energy constants
	public static final double NEURON_ENERGY = 0.00001; // Upkeep per neuron.
	public static final double FIRING_ENERGY = 0.00001; // Energy to fire each neuron.
	
	// Weight matrix and input and output vectors.
	private int neurons;
	private DoubleMatrix weights; // Inputs and outputs are stacked over neurons.
	private DoubleMatrix inputs;
	private DoubleMatrix outputs;
	
	public NBrain(NGene g) {
		weights = g.getWeights();
		neurons = g.getNeurons();
		inputs = new DoubleMatrix(INPUTS + neurons, 1);
		outputs = new DoubleMatrix(OUTPUTS + neurons, 1);
	}
	
	/**
	 * Input sensory information into the brain.
	 * 
	 * @param index Type of sense
	 * @param value Value of sense
	 */
	public void input(int index, double value) {
		if(index >= INPUTS || index < 0)
			return;
		
		inputs.put(index,value);
	}
	
	/**
	 * Read off action information from the brain.
	 * 
	 * @param index Type of action
	 * @return Value of action
	 */
	public double output(int index) {
		if(index >= OUTPUTS || index < 0)
			return 0;
		
		return outputs.get(index);
	}

	/**
	 * Ticks the brain. This is a three-step process.
	 * 1. Multiply inputs by weights to get temporary outputs.
	 * 2. Apply cutoff function to temporary outputs to get final outputs.
	 * 3. Copy neurons from final outputs back into inputs.
	 * Returns the energy used to tick.
	 * 
	 * @return energy
	 */
	public double tick() {
		
		double energy = 0;
		
		// Step 1.
		outputs = weights.mmul(inputs);
		energy += FIRING_ENERGY*outputs.norm1();
		
		// Step 2 and 3.
		for(int i = 0; i < neurons; i++) {
			inputs.put(INPUTS + i,linear(outputs.get(OUTPUTS + i)));
		}
		
		energy += NEURON_ENERGY*neurons;
		
		return energy;
	}
	
	// Cutoff functions for neurons.
	private double step(double x) {
		return x >= 1 ? 1 : 0;
	}
	
	private double linear(double x) {
		return (x >= 0) ? (x <= 1 ? x : 1) : 0;
	}

	public void print() {
		String string = weights.toString("%.1f");
		System.out.println(string);
	}

}
