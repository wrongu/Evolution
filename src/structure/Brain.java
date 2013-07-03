package structure;

import java.util.List;

import bio.ANN;


public class Brain {
	
	private List<Sense> inputs;
	private List<Muscle> outputs;
	private ANN neuralnet;
	
	public Brain(List<Sense> senses, List<Muscle> muscles) {
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
