package structure;

import java.util.List;

import bio.ann.ANN;
import bio.ann.ISense;


public class Brain {
	
	private List<ISense> inputs;
	private List<Muscle> outputs;
	private ANN neuralnet;
	
	public Brain(List<ISense> senses, List<Muscle> muscles) {
		inputs = senses;
		outputs = muscles;
	}
	
	public void update(){
		// TODO - read senses, process data, output to muscles
	}

	public double estimateSize(){
		// TODO - get a size estimate based on size of neural network?
		return 10.0;
	}
}
