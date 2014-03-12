package bio.organisms.brain;

import java.util.List;

import bio.ann.ANN;
import bio.organisms.Muscle;


public class Brain {
	
	private List<ISense> inputs;
	private List<Muscle> outputs;
	private ANN neuralnet;
	
	public Brain(List<ISense> senses, List<Muscle> muscles) {
		inputs = senses;
		outputs = muscles;
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
