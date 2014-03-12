package bio.organisms.brain;

import java.util.List;

import bio.ann.MatrixNeuralNet;

public class Brain {
	
	private MatrixNeuralNet network;
	private List<? extends ISense> inputs;
	private List<? extends IOutput> actions;

	public Brain(List<? extends ISense> senses, List<? extends IOutput> outputs) {
		inputs = senses;
		actions = outputs;
	}

	public void tick() {
		// TODO Auto-generated method stub
	}
	
	public void setInput(int id, double val){
		// TODO
	}
	
	public double getOutput(int id){
		// TODO
		return 0.0;
	}
}
