package neuralevo;

import org.jblas.DoubleMatrix;

public class NGene {
	
	// Mutation rate constants.
	public static final double META_WEIGHT_MUTATION_RATE = 0.01;
	public static final double META_NEURON_MUTATION_RATE = 0.01;
	public static final double DEFAULT_WEIGHT_MUTATION_RATE = 0.1;
	public static final double DEFAULT_NEURON_MUTATION_RATE = 0.5;
	
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
		weights.muli(1);
		
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
		for(int i = 0; i < dNeurons; i++) {
			neuronsToAdd += (Math.random() < 0.5) ? 0 : 1;
		}
		int neuronsToRemove = dNeurons - neuronsToAdd;
		int newNeurons = neurons + neuronsToAdd;
		
		// Randomly select extra neurons to add.
		boolean[] deleteRow = new boolean[NBrain.OUTPUTS + g.neurons];
		boolean[] deleteCol = new boolean[NBrain.INPUTS + g.neurons];
		boolean[] deleteNeurons = pickRandom(dNeurons, neuronsToRemove);
		for(int i = 0; i < dNeurons; i++) {
			deleteRow[NBrain.OUTPUTS + g.neurons - dNeurons + i] = deleteNeurons[i];
			deleteCol[NBrain.INPUTS + g.neurons - dNeurons + i] = deleteNeurons[i];
		}
		
		System.out.println(); // DEBUGGING
		DoubleMatrix gRemovedWeights = deleteRowsCols(g.weights,deleteRow, deleteCol);
		printMatrix(g.weights); // DEBUGGING
		printMatrix(gRemovedWeights); // DEBUGGING
		DoubleMatrix gRemovedWeightMut =  deleteRowsCols(g.weightMutationRates,deleteRow, deleteCol);
		
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
//		System.out.println("Change in neurons: " + changeInNeurons); // DEBUGGING
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
			
			// Multiply keep matrices to get a keep matrix with dimensions to match weight matrix.
			//DoubleMatrix keep = markToKeep(neurons, changeInNeurons, neurons);
			boolean[] deleteRow = new boolean[NBrain.OUTPUTS + neurons];
			boolean[] deleteCol = new boolean[NBrain.INPUTS + neurons];
			boolean[] deleteNeurons = pickRandom(neurons, changeInNeurons);
			for(int i = 0; i < neurons; i++) {
				deleteRow[i + NBrain.OUTPUTS] = deleteNeurons[i];
				deleteCol[i + NBrain.INPUTS] = deleteNeurons[i];
			}
			
			// Obtain the submatrix with the appropriate omitted rows and columns.
			newWeights = deleteRowsCols(weights,deleteRow,deleteCol);
			newWeightMutationRates = deleteRowsCols(weightMutationRates,deleteRow,deleteCol);
			
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
		int rows = 4;
		int cols = 5;
		int drows = 1;
		int dcols = 1;
		DoubleMatrix m = new DoubleMatrix(rows, cols);
		double entry = 0;
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				m.put(i,j,entry);
				entry += 0.1;
			}
		}
		
		System.out.println("Original Matrix: ");
		printMatrix(m);
		System.out.println();
		
		boolean[] removeRow = pickRandom(rows,drows);
		boolean[] removeCol = pickRandom(cols,dcols);
		
		System.out.println("Rows to Remove: ");
		for(int i = 0; i < rows; i++) {
			System.out.print(removeRow[i] ? "1 " : "0 ");
		}
		System.out.println();
		System.out.println("Cols to Remove: ");
		for(int i = 0; i < cols; i++) {
			System.out.print(removeCol[i] ? "1 " : "0 ");
		}
		System.out.println();
		System.out.println();
		
		System.out.println("New Matrix: ");
		printMatrix(deleteRowsCols(m,removeRow, removeCol));
	}
	
	private static void printMatrix(DoubleMatrix m) {
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
	
//	private static DoubleMatrix markToKeep(int n, int toRemove, int outOfLast) {
//		
//		if(n < 0) {
//			return null;
//		}
//		
//		if(outOfLast > n) {
//			outOfLast = n;
//		}
//		
//		if(toRemove > outOfLast) {
//			return DoubleMatrix.ones(NBrain.OUTPUTS, NBrain.INPUTS);
//		}
//		
//		DoubleMatrix keepRows = DoubleMatrix.ones(NBrain.OUTPUTS + n,1);
//		DoubleMatrix keepCols = DoubleMatrix.ones(1,NBrain.INPUTS + n);
//		
//		// Randomly decide which neurons to delete. Record choice in keep matrices.
//		for(int i = 0; i < toRemove; i++) {
//			int rand = (int)((outOfLast - i)*Math.random());
////			System.out.println("rand = " + rand);
//			int place = n - outOfLast;
////			System.out.println("starting place = " + place);
//			int j = 0;
//			System.out.println("Begin loop...");
//			while(j < rand) {
//				System.out.println("j = " + j);
//				System.out.println("place = " + place);
//				j += (keepRows.get(NBrain.OUTPUTS + place) == 1) ? 1 : 0;
//				place++;
//			}
//			System.out.println("...ending loop.");
//
//			keepRows.put(NBrain.OUTPUTS + place, 0);
//			keepCols.put(NBrain.INPUTS + place, 0);
//		}
//
//		// Multiply keep matrices to get a keep matrix with dimensions to match weight matrix.
//		return keepRows.mmul(keepCols);
//	}
	
	private static DoubleMatrix deleteRowsCols(DoubleMatrix m, boolean[] deleteRows, boolean[] deleteCols) {
		
		// Determine new matrix dimensions.
		int removedRows = 0;
		int removedCols = 0;
		for(int i = 0; i < deleteRows.length; i++) {
			removedRows += deleteRows[i] ? 1 : 0;
		}
		for(int j = 0; j < deleteCols.length; j++) {
			removedCols += deleteCols[j] ? 1 : 0;
		}
		DoubleMatrix newMatrix = DoubleMatrix.zeros(m.rows - removedRows, m.columns - removedCols);
		
		// Loop through copying entries form m to newMatrix, skipping the indicated rows and columns.
		int newi = 0;
		int newj = 0;
		for(int i = 0; i < m.rows; i++) {
			
			if(deleteRows[i])
				continue;
			
			for(int j = 0; j < m.columns; j++) {
				
				if(deleteCols[j])
					continue;
				
				newMatrix.put(newi, newj, m.get(i,j));
				newj++;
			}
			newj = 0;
			newi++;
		}
		
		return newMatrix;
	}
	
	private static boolean[] pickRandom(int n, int k) {
		// Value checks.
		if(n < 0) {
			return null;
		}
		k = (k < 0) ? 0 : ((k > n) ? k = n : k);
		
		boolean[] list = new boolean[n];
		
		// Pick k random integers of n.
		for(int i = 0; i < k; i++) {
			int pick = (int)(Math.random() * (n - i));
			int j = 0;
			int place = 0;
			for( ; j <= pick; place++) {
				if(list[place])
					continue;
				j++;
			}
			list[--place] = true;
//			System.out.println("pick = " + pick);
//			System.out.println("place = " + place);
		}
		
		return list;
	}

}
