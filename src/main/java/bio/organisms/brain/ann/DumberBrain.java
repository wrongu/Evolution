package bio.organisms.brain.ann;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.jblas.ranges.RangeUtils;

import environment.Environment;
import environment.RandomFoodEnvironment;

import bio.genetics.Gene;
import bio.organisms.AbstractOrganism;
import bio.organisms.SimpleCircleOrganism;
import bio.organisms.brain.IBrain;

public class DumberBrain  implements IBrain {
	
	// Energy constants
	public static final double NEURON_ENERGY = 0.01; // Upkeep per neuron.
	public static final double FIRING_ENERGY = 0.01; // Energy to fire each neuron.

	// necessary matrices
	private int i, s, o;
	private DoubleMatrix weightMatrix;
	private DoubleMatrix actVector;
	private DoubleMatrix inVector;
	private DoubleMatrix outVector;
	private DoubleMatrix thresholdVector;
	private DoubleMatrix restVector;

	//constants for updating, etc
	private double c = 5.0; // logistic function f = (1 + exp(-a*c))^-1;
	private double decay = 0.9;
	private boolean initialized = false;
	
	private Gene<DumberBrain> gene;
	// The organism who provides us energy
	private AbstractOrganism meatCase;
	
	public static DumberBrain fromGene(Gene<DumberBrain> gene, AbstractOrganism host){
		DumberBrain brain = gene.create(0, 0, null);
		brain.gene = gene;
		brain.meatCase = host;
		return brain;
	}
	
	public static DumberBrain newEmpty(int s, int o, AbstractOrganism host){
		return fromGene(new BrainGene(s, o), host);
	}
	
	public static DumberBrain newRandom(int s, int o, AbstractOrganism host, Random r){
		return fromGene(new BrainGene(s, o, r), host);
	}
	
	private DumberBrain(int i, int s, int o, double c, double decay){
		this.i = i;
		this.s = s;
		this.o = o;
		this.c = c;
		this.decay = decay;
		weightMatrix = new DoubleMatrix(i+o, i+s);
		actVector = DoubleMatrix.zeros(i+o, 1); // initialized for real in init()
		inVector = DoubleMatrix.zeros(s, 1);
		outVector = DoubleMatrix.zeros(i+o, 1);
		// no sense neurons here because they are treated as axon activations
		// (i.e. already threshold'd)
		thresholdVector = DoubleMatrix.zeros(i+o, 1);
		restVector = DoubleMatrix.zeros(i+o);
	}

	private void init(){
		actVector = restVector.dup(); // begin at rest
		initialized = true;
	}

	public void setProperties(int neuron, double threshold, double rest){
		assert(0 <= neuron && neuron < i+s);
		thresholdVector.put(neuron, threshold);
		if(neuron < i) restVector.put(neuron, rest);
	}

	public void tick(){
		if(!initialized) init();
		
		// multiplier is a combination of internal outputs and senses
		DoubleMatrix axonVector = DoubleMatrix.concatVertically(outVector.getRowRange(0, i, 0), inVector);
		
		// activation += weights * axons;
		actVector = actVector.add(weightMatrix.mmul(axonVector));
	
		// update outputs as logistic-activation-function of activation
		// out = 1 / (1 + exp(-c*a)) = (1 + exp(a)^-c)^-1
		//
		// note that actVector.sub(thresholdVector) is used in place of "a", above, so that thresholds may vary
		outVector =
				MatrixFunctions.pow(
						MatrixFunctions.pow(
								MatrixFunctions.expi(actVector.sub(thresholdVector)),
								-c)
							.add(1.0),
							-1.0);
		// decay activation towards rest values
		actVector = (actVector.sub(restVector).mul(decay)).add(restVector);
		
		// clear inputs now that they have been used
		inVector = DoubleMatrix.zeros(s, 1);
		
		// drain energy from host
		double energy = NEURON_ENERGY * i + FIRING_ENERGY * outVector.sum();
		meatCase.useEnergy(energy);
	}

	public String toString(){
		StringBuilder builder = new StringBuilder();

		builder.append("activation:\t");
		for(int i=0; i < actVector.length; i++)
			builder.append(actVector.get(i)+", ");
		builder.append('\n');

		builder.append("output:\t\t");
		for(int i=0; i < outVector.length; i++)
			builder.append(outVector.get(i)+", ");
		builder.append('\n');

		builder.append("thresholds:\t");
		for(int i=0; i < thresholdVector.length; i++)
			builder.append(thresholdVector.get(i)+", ");
		builder.append('\n');

		builder.append("rest:\t\t");
		for(int i=0; i < restVector.length; i++)
			builder.append(restVector.get(i)+", ");
		builder.append('\n');
		
//		builder.append("weights:\n");
//		for(int r = 0; r < weightMatrix.rows; r++){
//			for(int c = 0; c < weightMatrix.columns; c++){
//				builder.append(weightMatrix.get(r,c) + ", ");
//			}
//			builder.append('\n');
//		}

		return builder.toString();
	}

	public IBrain beget(Environment e, AbstractOrganism parent) {
		Gene<DumberBrain> child_gene = this.gene.mutate(e.getRandom());
		DumberBrain brain = child_gene.create(0, 0, e);
		brain.gene = child_gene;
		brain.meatCase = parent;
		return brain;
	}

	public void setInput(int id, double val) {
		assert(0 <= id && id < s);
		inVector.put(id, val);
	}

	public double getOutput(int id) {
		assert(0 <= id && id < o);
		return actVector.get(i+id);
	}

	public Gene<? extends IBrain> getGene() {
		return gene;
	}
	
	private static class BrainGene extends Gene<DumberBrain>{
		
		private static final String ADD_NEURON = "add";
		private static final String DEL_NEURON = "del";
		private static final String ALTER_CONNECTION = "conn";
		private static final String ALTER_THRESHOLD = "thresh";
		private static final String ALTER_REST = "rest";
		private static final String ALTER_DECAY = "dec";
		private static final String ALTER_SIGMOID = "exp";

		// copy of relevant (mutable) values in the DumberBrain
		private DoubleMatrix weightMatrix, thresholdVector, restVector;
		int i, s, o;
		private double exp, decay;
		
		public BrainGene(int s, int o){
			// registering meta-mutation parameters means that all their updates and serialization come
			// for free, courtesy of Gene<T>
			super(ADD_NEURON, DEL_NEURON, ALTER_CONNECTION,
				ALTER_THRESHOLD, ALTER_REST, ALTER_DECAY);
			// Generation Zero initialization: all zeros
			weightMatrix = DoubleMatrix.zeros(o, s);
			thresholdVector = DoubleMatrix.zeros(o, 1);
			restVector = DoubleMatrix.zeros(o, 1);
			this.i = 0;
			this.s = s;
			this.o = o;
			this.exp = 3.0;
			this.decay = 0.85;
		}
		
		public BrainGene(int s, int o, Random r){
			this(s, o);
			// random weights [-1, 1]
			for(int n=0; n<s*o; n++)
				weightMatrix.put(n, r.nextDouble() - r.nextDouble());
			// random thresholds [0, 1]
			for(int ti=0; ti<o; ti++)
				thresholdVector.put(ti, r.nextDouble());
			// random rest [0, 1] (less than threshold)
			for(int ri=0; ri<o; ri++)
				restVector.put(ri, r.nextDouble() * thresholdVector.get(ri));
		}
		
		private void addNeuron(){
			this.i++;
			// create a new matrix that is 1 row and 1 column larger
			DoubleMatrix expanded = new DoubleMatrix(i+o, i+s);
			// copy the old matrix into the [1:end] range (leave zeros in the 0th row and column)
			expanded.put(RangeUtils.interval(1, i+o), RangeUtils.interval(1,i+s), weightMatrix);
			weightMatrix = expanded;
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
					weightMatrix.getRange(0, which-1, 0, which-1));
			shrink.put(
					RangeUtils.interval(which+1, i+o),
					RangeUtils.interval(0, which-1),
					weightMatrix.getRange(which+1, i+o, 0, which-1));
			shrink.put(
					RangeUtils.interval(0, which-1),
					RangeUtils.interval(which+1, i+s),
					weightMatrix.getRange(0, which-1, which+1, i+s));
			shrink.put(
					RangeUtils.interval(which+1, i+o),
					RangeUtils.interval(which+1, i+s),
					weightMatrix.getRange(which+1, i+o, which+1, i+s));
		}
		
		private void alterConnection(int fro, int to, double val){
			weightMatrix.put(to, fro, val);
		}

		@Override
		public DumberBrain create(double posx, double posy, Environment e) {
			DumberBrain brain = new DumberBrain(i, s, o, exp, decay);
			brain.weightMatrix = this.weightMatrix.dup();
			brain.thresholdVector = this.thresholdVector.dup();
			brain.restVector = this.restVector.dup();
			return brain;
		}

		@Override
		protected void _serialize(DataOutputStream dest) throws IOException {
			dest.writeInt(i);
			dest.writeInt(s);
			dest.writeInt(o);
			dest.writeDouble(exp);
			dest.writeDouble(decay);
			for(int n = 0; n < weightMatrix.length; n++)
				dest.writeDouble(weightMatrix.get(n));
			for(int t = 0; t < thresholdVector.length; t++)
				dest.writeDouble(thresholdVector.get(t));
			for(int r = 0; r < restVector.length; r++)
				dest.writeDouble(restVector.get(r));
		}

		@Override
		protected void _deserialize(DataInputStream src) throws IOException {
			i = src.readInt();
			s = src.readInt();
			o = src.readInt();
			exp = src.readDouble();
			decay = src.readDouble();
			weightMatrix = new DoubleMatrix(i+o, i+s);
			for(int n = 0; n < weightMatrix.length; i++)
				weightMatrix.put(n, src.readDouble());
			thresholdVector = new DoubleMatrix(i+o, 1);
			for(int t = 0; t < thresholdVector.length; t++)
				thresholdVector.put(t, src.readDouble());
			restVector = new DoubleMatrix(i+o, 1);
			for(int r = 0; r < restVector.length; r++)
				restVector.put(r, src.readDouble());
		}

		@Override
		protected Gene<DumberBrain> _clone() {
			BrainGene g = new BrainGene(s, o);
			g.i = this.i;
			g.weightMatrix = this.weightMatrix.dup();
			g.thresholdVector = this.thresholdVector.dup();
			g.restVector = this.restVector.dup();
			g.exp = this.exp;
			g.decay = this.decay;
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
			for(int ti=0; ti<thresholdVector.length; ti++)
				if(r.nextDouble() < mutationRate(ALTER_THRESHOLD))
					// up to 10% change in either direction
					thresholdVector.put(ti, thresholdVector.get(ti) * (1 + (r.nextDouble() - r.nextDouble())*0.1));
			// CHANGE REST
			for(int ri=0; ri<restVector.length; ri++)
				if(r.nextDouble() < mutationRate(ALTER_THRESHOLD))
					// up to 10% change in either direction
					restVector.put(ri, restVector.get(ri) * (1 + (r.nextDouble() - r.nextDouble())*0.1));

			// CHANGE DECAY RATE
			if(r.nextDouble() < mutationRate(ALTER_DECAY)){
				// up to 10% change in either direction
				decay *= (1 + (r.nextDouble() - r.nextDouble())*0.1);
			}
			// CHANGE SIGMOID SENSITIVITY
			if(r.nextDouble() < mutationRate(ALTER_SIGMOID)){
				// up to 10% change in either direction
				exp *= (1 + (r.nextDouble() - r.nextDouble())*0.1);
			}
		}
		
	}

	public static void main(String[] args){
		Environment env = new RandomFoodEnvironment(1.0, 0L);
		SimpleCircleOrganism orgo = new SimpleCircleOrganism(env, 10.0, 0, 0);
		IBrain myAnn = DumberBrain.newRandom(2, 4, orgo, env.getRandom());

		myAnn.setInput(0, 1.0);
		myAnn.setInput(1, 1.0);

		System.out.println(myAnn);

		for(int i=0; i<5; i++){
			myAnn.tick();
			System.out.println(myAnn);
		}
		
		System.out.println("===== MUTATING =====");
		
		myAnn = myAnn.beget(env, orgo);
		
		System.out.println(myAnn);

		for(int i=0; i<5; i++){
			myAnn.tick();
			System.out.println(myAnn);
		}
	}
}
