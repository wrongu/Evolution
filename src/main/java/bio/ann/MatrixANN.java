package bio.ann;

import java.util.HashMap;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

public class MatrixANN {

	// necessary matrices
	private DoubleMatrix weightMatrix;
	private DoubleMatrix actVector;
	private DoubleMatrix outVector;
	private DoubleMatrix thresholdVector;
	private DoubleMatrix restVector;

	//constants for updating, etc
	private final double c = 5.0; // logistic function f = (1 + exp(-a*c))^-1;
	private final double decay = 0.9;

	// book-keeping
	private HashMap<Integer, Integer> id_map;
	private int next_ind = 0;

	public MatrixANN(int n_neurons){
		weightMatrix = new DoubleMatrix(n_neurons, n_neurons);
		actVector = DoubleMatrix.zeros(n_neurons);
		outVector = DoubleMatrix.zeros(n_neurons);	
		thresholdVector = DoubleMatrix.zeros(n_neurons);
		restVector = DoubleMatrix.zeros(n_neurons);

		id_map = new HashMap<Integer, Integer>(n_neurons);
	}

	public void init(){
		actVector = restVector.dup();
	}

	public void setProperties(int neuron_id, double threshold, double rest){
		int ind = id_to_index(neuron_id);
		thresholdVector.put(ind, threshold);
		restVector.put(ind, rest);
	}

	public void addConnection(int id_from, int id_to, double weight){
		int ind_from = id_to_index(id_from);
		int ind_to = id_to_index(id_to);
		weightMatrix.put(ind_to, ind_from, weight);
	}

	private int id_to_index(int id){
		Integer lookup = id_map.get(id);
		if(lookup != null) {
			//			System.out.println(id + " --> " + lookup);
			return lookup.intValue();
		} else {
			//			System.out.println(id + " --> " + next_ind);
			id_map.put(id, next_ind);
			return next_ind++;
		}
	}

	public void nextState(){
		// activation += weights * outputs;
		actVector = actVector.add(weightMatrix.mmul(outVector));
	
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
		// decay activation
		actVector = (actVector.sub(restVector).mul(decay)).add(restVector);
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

	public static void main(String[] args){
		MatrixANN myAnn = new MatrixANN(4);

		myAnn.setProperties(0, 0.0, 0.1);
		myAnn.setProperties(1, 0.0, 0.1);
		myAnn.setProperties(2, 0.0, 0.1);
		myAnn.setProperties(3, 0.0, 0.1);

		myAnn.addConnection(0, 1, 1.0);
		myAnn.addConnection(1, 2, -1.0);

		myAnn.init();

		System.out.println(myAnn);

		for(int i=0; i<5; i++){
			myAnn.nextState();
			System.out.println(myAnn);
		}
	}
}
