package neuralevo;

import org.jblas.DoubleMatrix;

public class NGene {
	
	// Mutation rate constants.
	public static final double META_WEIGHT_MUTATION_RATE = 0.01;
	public static final double META_NEURON_MUTATION_RATE = 0.01;
	public static final double DEFAULT_WEIGHT_MUTATION_RATE = 0.1;
	public static final double DEFAULT_NEURON_MUTATION_RATE = 0.3;
	
	// Mutation rate variables.
	private DoubleMatrix weightMutationRates;
	private double neuronMutationRate;
	
	// Brain information.
	private int neurons;
	private DoubleMatrix weights; // Inputs and outputs are stacked over neurons.
	
	/**
	 * Creates a deep copy of NGene g.
	 * 
	 * @param g NGene to be copied.
	 */
	public NGene(NGene g) {
		weightMutationRates = new DoubleMatrix().copy(g.weightMutationRates);
		neuronMutationRate = g.neuronMutationRate;
		neurons = g.neurons;
		weights = new DoubleMatrix().copy(g.weights);
	}
	
	/**
	 * Generates a random minimal gene.
	 */
	public NGene() {
		// Initialize mutation rates.
		weightMutationRates = DoubleMatrix.ones(NBrain.OUTPUTS, NBrain.INPUTS);
		weightMutationRates.muli(DEFAULT_WEIGHT_MUTATION_RATE);
		neuronMutationRate = DEFAULT_NEURON_MUTATION_RATE;
		
		// Initialize random weight.
		weights = DoubleMatrix.randn(NBrain.OUTPUTS, NBrain.INPUTS);
		weights.muli(2);
		
		// Set neurons to 0.
		neurons = 0;
	}
	
	/**
	 * Mutates weight matrix, number of neurons, and then the associated mutation rates.
	 */
	public void mutate() {
		// Mutate weight matrix including number of neurons and weights.
		mutateNeurons(); // Mutates neurons.
		weights.addi(DoubleMatrix.randn(NBrain.OUTPUTS + neurons, NBrain.INPUTS + neurons).mul(weightMutationRates)); // Mutates weights.
		
		// Meta-mutate mutation rates for weights and neurons.
		neuronMutationRate += META_NEURON_MUTATION_RATE*(DoubleMatrix.randn(1).scalar());
		weightMutationRates.addi(DoubleMatrix.randn(NBrain.OUTPUTS + neurons, NBrain.INPUTS + neurons).mul(META_WEIGHT_MUTATION_RATE));
	}
	
	/**
	 * Generates the offspring NGene from this NGene and another NGene.
	 * We might need to make this method smarter at some point. Right
	 * now, it is very naive.
	 * 
	 * @param g the other parent's NGene
	 * @return offspring's NGene
	 */
	public NGene cross(NGene g) {
		// First, resolve the discrepancies between number of neurons.
		// For simplicity, we assume WLOG that neurons <= g.neurons.
		if(g.neurons < neurons) {
			return g.cross(this);
		}
		
		// Make a new gene to modify.
		NGene newGene = new NGene(this);
		
		// Determine difference between number of neurons.
		int dNeurons = g.neurons - neurons;
		
		// Randomly determine how many neurons to add to the new gene.
		int neuronsToAdd = 0;
		for(int i = 0; i <= dNeurons; i++) {
			neuronsToAdd += (Math.random() < 0.5) ? 0 : 1;
		}
		int neuronsToRemove = dNeurons - neuronsToAdd;
		int newNeurons = neurons + neuronsToAdd;
		
		// Randomly select extra neurons to add.
		DoubleMatrix keep = markToKeep(neurons, neuronsToRemove, dNeurons);
		DoubleMatrix gRemovedWeights = g.weights.get(keep);
		DoubleMatrix gRemovedWeightMut = g.weightMutationRates.get(keep);
		gRemovedWeights.reshape(NBrain.OUTPUTS + newNeurons, NBrain.INPUTS + newNeurons);
		gRemovedWeightMut.reshape(NBrain.OUTPUTS + newNeurons, NBrain.INPUTS + newNeurons);
		
		// Resize newGene.weights 'n shit.
		newGene.weights = DoubleMatrix.concatHorizontally(newGene.weights, DoubleMatrix.zeros(NBrain.OUTPUTS + neurons,neuronsToAdd));
		newGene.weightMutationRates = DoubleMatrix.concatHorizontally(newGene.weightMutationRates, DoubleMatrix.zeros(NBrain.OUTPUTS + neurons,neuronsToAdd));
		newGene.weights = DoubleMatrix.concatVertically(newGene.weights, DoubleMatrix.zeros(neuronsToAdd, NBrain.INPUTS + newNeurons));
		newGene.weightMutationRates = DoubleMatrix.concatVertically(newGene.weightMutationRates, DoubleMatrix.zeros(neuronsToAdd, NBrain.INPUTS + newNeurons));
		newGene.neurons = newNeurons;
		
		// Interpolate randomly.
		newGene.neuronMutationRate += Math.random()*(g.neuronMutationRate - neuronMutationRate);
		// Generate interpolation matrices
		DoubleMatrix interpolation1 = DoubleMatrix.rand(NBrain.OUTPUTS + neurons, NBrain.INPUTS + neurons);
		DoubleMatrix interpolation2 = DoubleMatrix.rand(NBrain.OUTPUTS + neurons, NBrain.INPUTS + neurons);
		interpolation1 = DoubleMatrix.concatHorizontally(interpolation1, DoubleMatrix.ones(NBrain.OUTPUTS + neurons, neuronsToAdd));
		interpolation1 = DoubleMatrix.concatVertically(interpolation1, DoubleMatrix.ones(neuronsToAdd, NBrain.INPUTS + newNeurons));
		interpolation2 = DoubleMatrix.concatHorizontally(interpolation2, DoubleMatrix.ones(NBrain.OUTPUTS + neurons, neuronsToAdd));
		interpolation2 = DoubleMatrix.concatVertically(interpolation2, DoubleMatrix.ones(neuronsToAdd, NBrain.INPUTS + newNeurons));
		// Interpolate between weight and mutation rate matrices.
		newGene.weights.addi(interpolation1.mul(newGene.weights.sub(gRemovedWeights)));
		newGene.weightMutationRates.addi(interpolation2.mul(newGene.weightMutationRates.sub(gRemovedWeightMut)));
		
		return newGene;
	}
	
	/**
	 * Returns a new copy of the weight matrix in this object.
	 * 
	 * @return weights
	 */
	public DoubleMatrix getWeights() {
		return new DoubleMatrix().copy(weights);
	}
	
	private void mutateNeurons() {
		
		// Determine the change in the number of neurons.
		double dn = DoubleMatrix.randn(1).scalar();
		dn *= neuronMutationRate;
		int changeInNeurons = (int)Math.round(dn);
		if(changeInNeurons + neurons < 0) {
			changeInNeurons = -neurons;
		}
		int newNeurons = neurons + changeInNeurons;

		// If change is positive, add neurons. If negative, delete random neurons.
		if(changeInNeurons == 0) {
			return;
		}
		
		DoubleMatrix newWeights;
		DoubleMatrix newWeightMutationRates;
		if(changeInNeurons > 0) {
			// Concatenate rows and columns onto the weight matrix.
			newWeights = DoubleMatrix.concatHorizontally(weights, DoubleMatrix.zeros(NBrain.OUTPUTS + neurons, changeInNeurons));
			newWeights = DoubleMatrix.concatVertically(newWeights, DoubleMatrix.zeros(changeInNeurons, NBrain.INPUTS + newNeurons));
			
			newWeightMutationRates = DoubleMatrix.concatHorizontally(weightMutationRates, DoubleMatrix.zeros(NBrain.OUTPUTS + neurons, changeInNeurons));
			newWeightMutationRates = DoubleMatrix.concatVertically(newWeightMutationRates, DoubleMatrix.zeros(changeInNeurons, NBrain.INPUTS + newNeurons));
			
			weights = newWeights;
			weightMutationRates = newWeightMutationRates;
			neurons = newNeurons;
			return;
		} else {
			changeInNeurons = -changeInNeurons;
			
//			// Initialize 'keep' matrices.
//			DoubleMatrix keepRows = DoubleMatrix.ones(NBrain.OUTPUTS + neurons, 1);
//			DoubleMatrix keepCols = DoubleMatrix.ones(1, NBrain.INPUTS + neurons);
//			
//			// Randomly decide which neurons to delete. Record choice in keep matrices.
//			for(int i = changeInNeurons-1; i >= 0; i--) {
//				int rand = (int)(i*Math.random());
//				int place = 0;
//				int j = 0;
//				while(j <= rand) {
//					j += (keepRows.get(NBrain.OUTPUTS + place) == 0) ? 0 : 1;
//					place++;
//				}
//				place--;
//				
//				keepRows.put(NBrain.OUTPUTS + place, 0);
//				keepCols.put(NBrain.INPUTS + place, 0);
//			}
			
			// Multiply keep matrices to get a keep matrix with dimensions to match weight matrix.
			DoubleMatrix keep = markToKeep(neurons, changeInNeurons, neurons);
			
			// Obtain the submatrix with the appropriate omitted rows and columns.
			newWeights = weights.get(keep);
			newWeightMutationRates = weightMutationRates.get(keep);
			newWeights.reshape(NBrain.OUTPUTS + newNeurons, NBrain.INPUTS + newNeurons);
			newWeightMutationRates.reshape(NBrain.OUTPUTS + newNeurons, NBrain.INPUTS + newNeurons);
			
			weights = newWeights;
			weightMutationRates = newWeightMutationRates;
			neurons = newNeurons;
		}
		
	}
	
	public int getNeurons() {
		return neurons;
	}
	
	// Mutation test.
	public static void main(String[] args) {
		NGene gene = new NGene();
		gene.print();
		System.out.println();
		for(int i = 1; i <= 20; i++) {
			System.out.println("Mutation #" + i + ":");
			gene.mutate();
			gene.print();
			System.out.println();
		}
	}
	
	private void printMatrix(DoubleMatrix m) {
		for(int i = 0; i < m.rows; i++) {
			System.out.print("[  ");
			for(int j = 0; j < m.columns; j++) {
				System.out.format("%+.1f  ", m.get(i,j));
			}
			System.out.println("]");
		}
	}
	
	public void print() {
		System.out.println("# of neurons: " + neurons);
		System.out.println("Weights:");
		printMatrix(weights);
	}
	
	private DoubleMatrix markToKeep(int n, int toRemove, int outOfLast) {
		
		if(n < 0) {
			return null;
		}
		
		if(outOfLast > n) {
			outOfLast = n;
		}
		
		if(toRemove > outOfLast) {
			return DoubleMatrix.ones(NBrain.OUTPUTS, NBrain.INPUTS);
		}
		
		DoubleMatrix keepRows = DoubleMatrix.ones(NBrain.OUTPUTS + n,1);
		DoubleMatrix keepCols = DoubleMatrix.ones(1,NBrain.INPUTS + n);
		
		// Randomly decide which neurons to delete. Record choice in keep matrices.
		for(int i = 0; i < toRemove; i--) {
			int rand = (int)((outOfLast - i)*Math.random());
			int place = n - outOfLast;
			int j = 0;
			while(j <= rand) {
				j += (keepRows.get(NBrain.OUTPUTS + place) == 0) ? 1 : 0;
				place++;
			}
			place--;

			keepRows.put(NBrain.OUTPUTS + place, 0);
			keepCols.put(NBrain.INPUTS + place, 0);
		}

		// Multiply keep matrices to get a keep matrix with dimensions to match weight matrix.
		return keepRows.mmul(keepCols);
	}

}
