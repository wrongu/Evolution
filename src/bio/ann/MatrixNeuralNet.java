package bio.ann;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import org.jblas.DoubleMatrix;
import org.jblas.ranges.RangeUtils;

import environment.Environment;

import bio.genetics.Gene;
import bio.organisms.AbstractOrganism;

/**
 * @author ewy-man and wrongu
 */
public class MatrixNeuralNet {
	
	// Energy constants
	public static final double NEURON_ENERGY = 0.00001; // Upkeep per neuron.
	public static final double FIRING_ENERGY = 0.00001; // Energy to fire each neuron.
	
	// Weight matrix and state vectors
	private int i, s, o;
	private DoubleMatrix W; // weight matrix (I+O) x (I+S)
	private DoubleMatrix A; // vector of current _internal_ activations (NOT outputs)
	private DoubleMatrix I; // vector of sensory inputs AND internal-outputs (staging for tick())
	private DoubleMatrix O; // vector of action potential outputs (stored after tick() for getOutput())
	private double threshold, action_potential, depolarize, decay;
	
	// A gene for evolution
	private Gene<MatrixNeuralNet> gene;
	// The organism who provides us energy
	private AbstractOrganism meatCase;
	
	public static MatrixNeuralNet fromGene(Gene<MatrixNeuralNet> g, AbstractOrganism org){
		// TODO not all genes interact with the environment.. get rid of args to create()
		MatrixNeuralNet net = g.create(0, 0, null);
		net.gene = g;
		net.meatCase = org;
		return net;
	}
	
	public static MatrixNeuralNet newEmpty(int s, int o, AbstractOrganism org){
		MatrixNeuralNet mnn = new MatrixNeuralNet(0, s, o, 0.1, 1.0, -0.1, 0.5);
		mnn.gene = mnn.new BrainGene();
		mnn.meatCase = org;
		return mnn;
	}
	
	/**
	 * Create a new neural net.
	 * @param i number of internal neurons
	 * @param s number of sensory neurons
	 * @param o number of output neurons
	 * @param th threshold for action potentials
	 * @param ap value of action potential
	 * @param de value of depolarization
	 */
	private MatrixNeuralNet(int i, int s, int o, double th, double ap, double dp, double dc){
		this.i = i;
		this.o = o;
		this.s = s;
		W = new DoubleMatrix(i+o, i+s);
		A = new DoubleMatrix(i+o, 1);
		I = new DoubleMatrix(i+s, 1);
		O = new DoubleMatrix(o, 1);
		threshold = th;
		action_potential = ap;
		depolarize = dp;
		decay = dc;
	}
	
	/**
	 * Get number of inputs
	 */
	public int inputs(){
		return s;
	}
	
	/**
	 * Get number of outputs
	 */
	public int outputs(){
		return o;
	}
	
	/**
	 * Get number of internal neurons
	 */
	public int neurons(){
		return i;
	}
	
	/**
	 * Input sensory information into the brain.
	 * 
	 * @param index which of the s inputs to set
	 * @param value Value of sense
	 */
	public void setInput(int index, double value) {
		if(index >= i || index < 0){
			System.err.println("MatrixNeuralNet.setInput() out of range");
			return;
		}
		A.put(index,value);
	}
	
	/**
	 * Read off action information from the brain.
	 * 
	 * @param index which of the o outputs to get
	 * @return Value of action
	 */
	public double getOutput(int index) {
		if(index >= o || index < 0){
			System.err.println("MatrixNeuralNet.getOutput() out of range");
			return 0.0;
		}
		return O.get(index);
	}

	/**
	 * Ticks the brain. This is a five-step process.
	 * 1. Decay all signals
	 * 2. Multiply inputs by weights to get next-activation
	 * 3. Apply thresholding and depolarization to get next-outputs
	 * 4. Clear input signals
	 * 5. Drain requisite energy from the organism
	 */
	public void tick() {
		// Step 1.
		A.mul(this.decay);
		I.mul(this.decay); // TODO handle this such that senses aren't decayed?
		O.mul(this.decay);
		// Step 2.
		A.add(W.mmul(I));
		// Step 3.
		for(int n=0; n < (i+o); n++){
			// first 'i' neurons are stored in I
			if(n < i){
				if(A.get(n) > threshold){
					I.put(n, action_potential);
					A.put(n, depolarize);
				}
			}
			// output neurons 'i+1:end' are stored in O
			else{
				if(A.get(n) > threshold){
					O.put(n-i, action_potential);
					A.put(n, depolarize);
				}
			}
		}
		// step 4. clear inputs
		for(int n=i+1; n<i+s; n++){
			I.put(n, 0.0);
		}
		// step 5;
		double energy = NEURON_ENERGY * i + FIRING_ENERGY * O.norm1();
		this.meatCase.useEnergy(energy);
	}

	public void print() {
		System.out.println(W.toString("%.1f"));
	}
	
	private class BrainGene extends Gene<MatrixNeuralNet>{
		
		private static final String ADD_NEURON = "add";
		private static final String DEL_NEURON = "del";
		private static final String ALTER_THRESHOLD = "thresh";
		private static final String ALTER_POTENTIAL = "pot";
		private static final String ALTER_DEPOLARIZE = "dep";
		private static final String ALTER_DECAY = "dec";
				
		public BrainGene(){
			// registering metamutation parameters means that all their updates and serialization come
			// for free, courtesy of Gene<T>
			this.initMutables(
					ADD_NEURON, DEL_NEURON,
					ALTER_THRESHOLD, ALTER_POTENTIAL,
					ALTER_DEPOLARIZE, ALTER_DECAY);
		}
		
		@Override
		public Gene<MatrixNeuralNet> mutate(Random r) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public MatrixNeuralNet create(double posx, double posy, Environment e) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected void sub_serialize(DataOutputStream s) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void sub_deserialize(DataInputStream s) throws IOException {
			// TODO Auto-generated method stub
			
		}
		
	}
}

