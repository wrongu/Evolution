package ann;

import java.util.ArrayList;
import java.util.List;

public class ANN {
	
	private List<Neuron> neurons;
	private List<NeuronConnection> connections;
	
	public ANN(){
		neurons = new ArrayList<Neuron>();
		connections = new ArrayList<NeuronConnection>();
	}
	
	public void addNeuron(Neuron n){
		addNeuron(n, Neuron.TYPE_HIDDEN);
	}
	
	public void addNeuron(Neuron n, int type){
		n.setType(type);
		neurons.add(n);
	}
	
	public void addConnection(Neuron a, Neuron b, float weight){
		addConnection(new NeuronConnection(weight, a, b));
	}
	
	public void addConnection(NeuronConnection conn){
		connections.add(conn);
		if(!neurons.contains(conn.getFrom())) neurons.add(conn.getFrom());
		if(!neurons.contains(conn.getTo()))   neurons.add(conn.getTo());
	}
	
	public void nextState(){
		for(NeuronConnection nc : connections)
			nc.activate();
		for(Neuron n : neurons)
			n.flush();
	}
}