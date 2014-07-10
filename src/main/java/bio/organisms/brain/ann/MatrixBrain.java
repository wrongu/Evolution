package bio.organisms.brain.ann;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import org.jblas.DoubleMatrix;
import org.jblas.ranges.RangeUtils;

import utils.Util;
import environment.Environment;
import applet.Config;
import bio.genetics.Gene;
import bio.organisms.AbstractOrganism;
import bio.organisms.brain.IBrain;

public class MatrixBrain implements IBrain {
	
	// Constants
	private static final double UPKEEP = Config.instance.getDouble("MATRIX_UPKEEP");
	private static final double ENERGY_PER_FIRE = Config.instance.getDouble("MATRIX_FIRE");
	
	// Things for the running brain
	private int numOutputs;
	private int numInputs;
	private int numNeurons;
	private DoubleMatrix weights;
	private DoubleMatrix inputs;
	private DoubleMatrix outputs;
	
	// The gene from which this brain is constructed.
	private BrainGene gene;
	
	// Generates a brain from a gene.
	private MatrixBrain(BrainGene gene) {
		this.gene = gene;
		numOutputs = gene.getNumOutputs();
		numInputs = gene.getNumInputs();
		numNeurons = gene.getNumNeurons();
		weights = gene.getWeights();
		inputs = new DoubleMatrix(numInputs + numNeurons,1);
		outputs = new DoubleMatrix(numOutputs + numNeurons,1);
		
		// Do a check!
		if((numOutputs + numNeurons != weights.rows) || (numInputs + numNeurons != weights.columns)) {
			System.err.println("Weight matrix rows and columns do not match outputs/inputs/neurons.");
			System.exit(0);
		}
	}
	
	// Generates a random brain with no neurons.
	public static MatrixBrain newRandom(int numInputs, int numOutputs) {
		BrainGene gene = BrainGene.randomGene(numInputs, numOutputs);
		return new MatrixBrain(gene);
	}
	
	@Override
	public IBrain beget(Environment e, AbstractOrganism parent) {
		return new MatrixBrain((BrainGene)gene.mutate());
	}

	@Override
	public double tick() {
		
		// Tick.
		outputs = weights.mmul(inputs);
		
		// Compute energy.
		double energy = 0;
		energy += ENERGY_PER_FIRE*outputs.norm1();
		energy += UPKEEP*numNeurons;
		
		// Apply cutoff function to neurons and outputs.
		for(int i = 0; i < outputs.length; i++) {
			outputs.put(i,cutoff(outputs.get(i)));
		}
		
		// Put neuron information back into input.
		for(int i = 0; i < numNeurons; i++) {
			inputs.put(i + numInputs, outputs.get(i + numOutputs));
		}
		
		return energy;
	}

	@Override
	public void setInput(int id, double val) {
		if(id >= 0 && id < numInputs) {
			inputs.put(id,val);
		}
	}

	@Override
	public double getOutput(int id) {
		if(id >= 0 && id < numOutputs) {
			return outputs.get(id);
		}
		return 0;
	}

	@Override
	public Gene<? extends IBrain> getGene() {
		return gene;
	}
	
	// Edit this function if you want to change the cutoff in tick().
	private double cutoff(double x) {
		return x < 0.5 ? 0 : (x > 1 ? 1 : 2*x - 1 );
	}
	
	//////////
	// Gene //
	//////////
	private static class BrainGene extends Gene<MatrixBrain> {
		
		private static final String ADD_NEURON = "add";
		private static final String DEL_NEURON = "del";
		private static final String ALTER_CONNECTION = "conn";
		private static final String ALTER_CONNECTION_CHANCE = "connchan";
		
		private static final double MUT_ADD_NEURON = Config.instance.getDouble("MATRIX_MUT_ADD_NEURON");
		private static final double MUT_DEL_NEURON = Config.instance.getDouble("MATRIX_MUT_DEL_NEURON");
		private static final double MUT_ALTER_CONNECTION = Config.instance.getDouble("MATRIX_MUT_ALTER_CONNECTION");
		private static final double MUT_ALTER_CONNECTION_CHANCE = Config.instance.getDouble("MATRIX_MUT_ALTER_CONNECTION_CHANCE");
		
		private int numOutputs;
		private int numInputs;
		private int numNeurons;
		private DoubleMatrix weights;
		
		public static BrainGene randomGene(int numInputs, int numOutputs) {
			BrainGene gene = new BrainGene(numInputs,numOutputs);
			gene.randomize();
			return gene;
		}
		
		public BrainGene(int numInputs, int numOutputs) {
			super(ADD_NEURON, DEL_NEURON, ALTER_CONNECTION, ALTER_CONNECTION_CHANCE);
			setMutationRate(ADD_NEURON,MUT_ADD_NEURON);
			setMutationRate(DEL_NEURON,MUT_DEL_NEURON);
			setMutationRate(ALTER_CONNECTION,MUT_ALTER_CONNECTION);
			setMutationRate(ALTER_CONNECTION_CHANCE,MUT_ALTER_CONNECTION_CHANCE);
			
			this.numOutputs = numOutputs;
			this.numInputs = numInputs;
			this.numNeurons = 0;
			weights = DoubleMatrix.zeros(numOutputs + numNeurons, numInputs + numNeurons);
			for(int i = 0; i < weights.length; i++) {
				weights.put(i,0);
			}
		}
		
		public BrainGene(BrainGene gene) {
			super(ADD_NEURON, DEL_NEURON, ALTER_CONNECTION, ALTER_CONNECTION_CHANCE);
			setMutationRate(ADD_NEURON, gene.mutationRate(ADD_NEURON));
			setMutationRate(DEL_NEURON, gene.mutationRate(DEL_NEURON));
			setMutationRate(ALTER_CONNECTION, gene.mutationRate(ALTER_CONNECTION));
			setMutationRate(ALTER_CONNECTION_CHANCE, gene.mutationRate(ALTER_CONNECTION_CHANCE));
			
			this.numOutputs = gene.numOutputs;
			this.numInputs = gene.numInputs;
			this.numNeurons = gene.numNeurons;
			this.weights = gene.weights.dup();
		}
		
		private void randomize() {
			for(int i = 0; i < weights.length; i++) {
				weights.put(i, Util.random.nextGaussian());
			}
		}
		
		public DoubleMatrix getWeights() {
			return weights.dup();
		}

		public int getNumNeurons() {
			return numNeurons;
		}

		public int getNumInputs() {
			return numInputs;
		}

		public int getNumOutputs() {
			return numOutputs;
		}

		@Override
		protected Gene<MatrixBrain> _clone() {
			return new BrainGene(this);
		}

		@Override
		protected void _mutate() {
			addNeurons();
			delNeurons();
			alterWeights();
		}

		@Override
		public MatrixBrain create(double posx, double posy, Environment e) {
			return new MatrixBrain(this);
		}

		@Override
		protected void _serialize(DataOutputStream s) throws IOException {
			
		}

		@Override
		protected void _deserialize(DataInputStream s) throws IOException {
			
		}
		
		private void addNeurons() {
			int n = 10;
			int neuronsToAdd = 0;
			
			for(int i = 0; i < n; i++) {
				neuronsToAdd += Util.random.nextDouble() < mutationRate(ADD_NEURON)/n ? 1 : 0;
			}
			
			DoubleMatrix newWeights = new DoubleMatrix(
					numOutputs + numNeurons + neuronsToAdd,
					numInputs + numNeurons + neuronsToAdd);
			
			newWeights.put(DoubleMatrix.ones(numOutputs + numNeurons, numInputs + numNeurons),weights);
			
			numNeurons += neuronsToAdd;
			weights = newWeights;
		}
		
		private void delNeurons() {
			int n = 10;
			int neuronsToDelete = 0;
			
			for(int i = 0; i < n; i++) {
				neuronsToDelete += Util.random.nextDouble() < mutationRate(DEL_NEURON)/n ? 1 : 0;
			}
			
			neuronsToDelete = Math.min(neuronsToDelete, numNeurons);
			
			int[] order = Util.randomOrder(numNeurons);
			DoubleMatrix rowMarkers = DoubleMatrix.ones(numOutputs + numNeurons,1);
			DoubleMatrix colMarkers = DoubleMatrix.ones(1,numInputs + numNeurons);
			
			for(int i = 0; i < neuronsToDelete; i++) {
				rowMarkers.put(numOutputs + order[i], 0);
				colMarkers.put(numInputs + order[i], 0);
			}
			
			weights = weights.get(rowMarkers,colMarkers).reshape(
					numOutputs + numNeurons - neuronsToDelete,
					numInputs + numNeurons - neuronsToDelete);
			
			numNeurons -= neuronsToDelete;
		}
		
		private void alterWeights() {
			for(int i = 0; i < weights.length; i++) {
				if(Util.random.nextDouble() < mutationRate(ALTER_CONNECTION_CHANCE)) {
					weights.put( i, weights.get(i) 
							+ mutationRate(ALTER_CONNECTION)*Util.random.nextGaussian());
				}
			}
		}
		
	}

}
