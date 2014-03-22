package bio.organisms.brain.ann;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import org.jblas.DoubleMatrix;
import org.jblas.ranges.RangeUtils;

import environment.Environment;
import environment.RandomFoodEnvironment;

import bio.genetics.Gene;
import bio.organisms.AbstractOrganism;
import bio.organisms.SimpleCircleOrganism;
import bio.organisms.brain.IBrain;

/**
 * @author wrongu
 */
public class DumbBrain implements IBrain {
	
	// Energy constants
	public static final double NEURON_ENERGY = 0.01; // Upkeep per neuron.
	public static final double FIRING_ENERGY = 0.01; // Energy to fire each neuron.
	
	// Weight matrix and state vectors
	private int i, s, o;
	private DoubleMatrix W; // weight matrix (I+O) x (I+S)
	private DoubleMatrix A; // vector of current _internal_ activations (NOT outputs)
	private DoubleMatrix I; // vector of sensory inputs AND internal-outputs (staging for tick())
	private DoubleMatrix O; // vector of action potential outputs (stored after tick() for getOutput())
	private double threshold, action_potential, depolarize, decay;
	
	// DEBUG
	private int generation = 0;
	
	// A gene for evolution
	private Gene<DumbBrain> gene;
	// The organism who provides us energy
	private AbstractOrganism meatCase;
	
	public static DumbBrain fromGene(Gene<DumbBrain> g, AbstractOrganism org){
		// TODO not all genes interact with the environment.. get rid of args to create()
		DumbBrain brain = g.create(0, 0, null);
		brain.gene = g;
		brain.meatCase = org;
		return brain;
	}
	
	public static DumbBrain newEmpty(int s, int o, AbstractOrganism org){
		return fromGene(new BrainGene(s, o), org);
	}
	
	public static DumbBrain newRandom(int s, int o, AbstractOrganism org, Random r){
		return fromGene(new BrainGene(s, o, r), org);
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
	private DumbBrain(int i, int s, int o, double th, double ap, double dp, double dc){
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
		if(index >= s || index < 0){
			System.err.println("DumbBrain.setInput() out of range: "+index+" ("+s+" inputs available)");
			return;
		}
		I.put(i+index,value);
	}
	
	/**
	 * Read off action information from the brain.
	 * 
	 * @param index which of the o outputs to get
	 * @return Value of action
	 */
	public double getOutput(int index) {
		if(index >= o || index < 0){
			System.err.println("DumbBrain.getOutput() out of range: "+index+" ("+o+" outputs available)");
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
		A = A.mul(this.decay);
		I = I.mul(this.decay); // TODO handle this such that senses aren't decayed?
		O = O.mul(this.decay);
		// Step 2.
		A = A.add(W.mmul(I));
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
		for(int n=i; n<i+s; n++){
			I.put(n, 0.0);
		}
		// step 5;
		double energy = NEURON_ENERGY * i + FIRING_ENERGY * O.norm1();
		this.meatCase.useEnergy(energy);
	}

	public IBrain beget(Environment e, AbstractOrganism parent) {
		Gene<DumbBrain> child_gene = this.gene.mutate(e.getRandom());
		DumbBrain brain = child_gene.create(0, 0, e);
		brain.gene = child_gene;
		brain.generation = this.generation + 1;
		brain.meatCase = parent;
		return brain;
	}

	public Gene<? extends IBrain> getGene() {
		return gene;
	}

	public String toString() {
		return "W: "+W.toString("%.1f") + "\nA: " + A.toString("%.1f") + "\nO: " + O.toString("%.1f");
	}
	
	private static class BrainGene extends Gene<DumbBrain>{
		
		private static final String ADD_NEURON = "add";
		private static final String DEL_NEURON = "del";
		private static final String ALTER_CONNECTION = "conn";
		private static final String ALTER_THRESHOLD = "thresh";
		private static final String ALTER_POTENTIAL = "pot";
		private static final String ALTER_DEPOLARIZE = "dep";
		private static final String ALTER_DECAY = "dec";

		// copy of relevant (mutable) values in the DumbBrain
		private DoubleMatrix W;
		int i, s, o;
		private double threshold, action_potential, depolarize, decay;
		
		public BrainGene(int s, int o){
			// registering meta-mutation parameters means that all their updates and serialization come
			// for free, courtesy of Gene<T>
			super(ADD_NEURON, DEL_NEURON, ALTER_CONNECTION,
				ALTER_THRESHOLD, ALTER_POTENTIAL,
				ALTER_DEPOLARIZE, ALTER_DECAY);
			W = new DoubleMatrix(o, s);
			this.i = 0;
			this.s = s;
			this.o = o;
			this.action_potential = 1.0;
			this.depolarize = -0.1;
			this.decay = 0.6;
			this.threshold = 0.0;
		}
		
		public BrainGene(int s, int o, Random r) {
			this(s, o);
			// random weights
			for(int n=0; n<W.length; n++)
				W.put(n, r.nextDouble()-r.nextDouble());
		}

		private void addNeuron(){
			this.i++;
			// create a new matrix that is 1 row and 1 column larger
			DoubleMatrix expanded = new DoubleMatrix(i+o, i+s);
			// copy the old matrix into the [1:end] range (leave zeros in the 0th row and column)
			expanded.put(RangeUtils.interval(1, i+o), RangeUtils.interval(1,i+s), this.W);
			this.W = expanded;
		}
		
		private void delNeuron(int which){
			assert(0 <= which && which < i);
			i--;
			// Make a new matrix to hold the data
			DoubleMatrix shrink = new DoubleMatrix(i+o, i+s);
			// Copy in 4 quadrants of data that are split by del_ind
			shrink.put(
					RangeUtils.interval(0, which-1),
					RangeUtils.interval(0, which-1),
					W.getRange(0, which-1, 0, which-1));
			shrink.put(
					RangeUtils.interval(which+1, i+o),
					RangeUtils.interval(0, which-1),
					W.getRange(which+1, i+o, 0, which-1));
			shrink.put(
					RangeUtils.interval(0, which-1),
					RangeUtils.interval(which+1, i+s),
					W.getRange(0, which-1, which+1, i+s));
			shrink.put(
					RangeUtils.interval(which+1, i+o),
					RangeUtils.interval(which+1, i+s),
					W.getRange(which+1, i+o, which+1, i+s));
		}
		
		private void alterConnection(int fro, int to, double val){
			W.put(to, fro, val);
		}

		@Override
		public DumbBrain create(double posx, double posy, Environment e) {
			DumbBrain brain = new DumbBrain(i, s, o, threshold, action_potential, depolarize, decay);
			brain.W = this.W.dup();
			return brain;
		}

		@Override
		protected void _serialize(DataOutputStream dest) throws IOException {
			dest.writeInt(i);
			dest.writeInt(s);
			dest.writeInt(o);
			for(int n = 0; n < W.length; n++)
				dest.writeDouble(W.get(n));
			dest.writeDouble(action_potential);
			dest.writeDouble(threshold);
			dest.writeDouble(decay);
			dest.writeDouble(depolarize);
		}

		@Override
		protected void _deserialize(DataInputStream src) throws IOException {
			i = src.readInt();
			s = src.readInt();
			o = src.readInt();
			W = new DoubleMatrix(i+o, i+s);
			for(int n = 0; n < W.length; i++)
				W.put(n, src.readDouble());
			action_potential = src.readDouble();
			threshold = src.readDouble();
			decay = src.readDouble();
			depolarize = src.readDouble();
		}

		@Override
		protected Gene<DumbBrain> _clone() {
			BrainGene g = new BrainGene(s, o);
			g.i = this.i;
			g.W = this.W.dup();
			g.threshold = this.threshold;
			g.action_potential = this.action_potential;
			g.decay = this.decay;
			g.depolarize = this.depolarize;
			return g;
		}

		@Override
		protected void _mutate(Random r) {
			// ADD 0 OR MORE
			int safe_limit = 0; // it's possible for mutation rates to get up to 1.0.. just in case, don't make an infinite loop!
			while(r.nextDouble() < mutationRate(ADD_NEURON) && (safe_limit++) < 100){
				addNeuron();
			}
			// REMOVE 0 OR MORE
			while(r.nextDouble() < mutationRate(DEL_NEURON) && i > 0){
				// choose a random index to remove, then decrement i
				int del_ind = r.nextInt(i);
				delNeuron(del_ind);
			}
			// CHANGE CONNECTION
			for(int fro = 0; fro < i+s; fro++){
				for(int to = 0; to < i+o; to++){
					if(r.nextDouble() < mutationRate(ALTER_CONNECTION)){
						alterConnection(fro, to, r.nextDouble()-r.nextDouble());
					}
				}
			}
			// CHANGE THRESHOLD
			if(r.nextDouble() < mutationRate(ALTER_THRESHOLD)){
				// up to 10% change in either direction
				threshold *= (1 + (r.nextDouble() - r.nextDouble())*0.1);
			}
			// CHANGE DEPOLARIZATION
			if(r.nextDouble() < mutationRate(ALTER_DEPOLARIZE)){
				// up to 10% change in either direction
				depolarize *= (1 + (r.nextDouble() - r.nextDouble())*0.1);
			}
			// CHANGE ACTION POTENTIAL
			if(r.nextDouble() < mutationRate(ALTER_POTENTIAL)){
				// up to 10% change in either direction
				action_potential *= (1 + (r.nextDouble() - r.nextDouble())*0.1);
			}
			// CHANGE DECAY RATE
			if(r.nextDouble() < mutationRate(ALTER_DECAY)){
				// up to 10% change in either direction
				decay *= (1 + (r.nextDouble() - r.nextDouble())*0.1);
			}
		}
		
	}
	
	// TESTING
	public static void main(String[] args){
		Environment e = new RandomFoodEnvironment(1.0, 12L);
		AbstractOrganism org = new SimpleCircleOrganism(e, 10.0, 0, 0);
		DumbBrain db0 = DumbBrain.newEmpty(2, 4, org);
		System.out.println(db0);
		DumbBrain db1 = DumbBrain.newRandom(2, 4, org, e.getRandom());
		System.out.println(db1);
		db1.setInput(0, 10.0);
		db1.setInput(1, 10.0);
		db1.tick();
		System.out.println(db1);
	}
}

